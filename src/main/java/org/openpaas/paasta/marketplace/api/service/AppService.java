package org.openpaas.paasta.marketplace.api.service;

import org.cloudfoundry.client.v2.applications.ListApplicationsRequest;
import org.cloudfoundry.client.v2.applications.ListApplicationsResponse;
import org.cloudfoundry.client.v2.buildpacks.BuildpackEntity;
import org.cloudfoundry.client.v2.buildpacks.BuildpackResource;
import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksRequest;
import org.cloudfoundry.client.v2.routemappings.CreateRouteMappingRequest;
import org.cloudfoundry.client.v2.routemappings.CreateRouteMappingResponse;
import org.cloudfoundry.client.v2.routes.CreateRouteRequest;
import org.cloudfoundry.client.v2.routes.CreateRouteResponse;
import org.cloudfoundry.client.v3.*;
import org.cloudfoundry.client.v3.applications.*;
import org.cloudfoundry.client.v3.builds.CreateBuildRequest;
import org.cloudfoundry.client.v3.builds.CreateBuildResponse;
import org.cloudfoundry.client.v3.builds.GetBuildRequest;
import org.cloudfoundry.client.v3.builds.GetBuildResponse;
import org.cloudfoundry.client.v3.packages.*;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.openpaas.paasta.marketplace.api.common.Common;
import org.openpaas.paasta.marketplace.api.common.CommonService;
import org.openpaas.paasta.marketplace.api.model.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.FileSystems;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


/**
 * App Service
 *
 * @author hrjin
 * @version 1.0
 * @since 2019-01-15
 */
@Service
public class AppService extends Common{
    private static final Logger LOGGER = LoggerFactory.getLogger(AppService.class);

    @Value("${local.uploadPath}")
    private String localUploadPath;

    @Autowired
    CommonService commonService;

    // TODO ::: 추후 build_pack 이름 받아와야 함.
    private static final String buildPackName =  "java_buildpack_offline";

    /**
     * 관리자 계정으로 Application 목록 조회.
     *
     * @return ListApplicationsResponse
     */
    public ListApplicationsResponse getAppsList() {
        ReactorCloudFoundryClient cloudFoundryClient = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken()));

        // Todo ::: organizationId 와 spaceId 는 추후 변수로 넣어줄 예정.
        ListApplicationsResponse listApplicationsResponse = cloudFoundryClient.applicationsV2().list(ListApplicationsRequest.builder().organizationId("9cffc61f-370b-4bbf-b091-45d937802d88").spaceId("05d7f1f8-686f-43d5-aa1a-01693733e36c").build()).block();
        LOGGER.info("어떻게 나오니? 앱 리스트야? " + listApplicationsResponse.toString());

        return listApplicationsResponse;
    }


    /**
     * Application 생성.
     *
     * 1. Find the GUID of your space and set an APP_NAME
     * ★ 2. Create an empty app
     * ★ 3. Create an empty package for the app
     * 4. If your package is type buildpack, create a ZIP file of your application (zip -r my-app.zip *)
     * ★ 5. If your package is type buildpack, upload your bits to your new package
     * ★ 6. Stage your package and create a build
     * 7. Wait for the state of the new build to reach STAGED (watch cf curl /v3/builds/$BUILD_GUID)
     * ★ 8. Get the droplet corresponding to the staged build
     * ★ 9. Assign the droplet to the app
     * ★ 10. Create a route
     * ★ 11. Map the route to your app
     * ★ 12. Start your app
     *
     *
     * @param app the app
     * @return App
     */
    public void createApp(App app){

        // 빈 App 생성 후 appGuid 조회
        String appGuid = createEmptyApp(app).getId();
        LOGGER.info("appGuid = " + appGuid);

        // 빈 package 생성 후 packageGuid 조회
        String packageGuid = createEmptyPackage(appGuid).getId();
        LOGGER.info("packageGuid = " + packageGuid);

        // 파일 및 빌드팩 포함한 package 업로드
        uploadPackage(packageGuid);

        try {
            BuildpackResource buildpackResource = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).buildpacks()
                    .list(ListBuildpacksRequest.builder().build()).block()
                    .getResources().stream().filter(r -> r.getEntity().getName().equals(buildPackName)).collect(Collectors.toList()).get(0);
            LOGGER.info(buildpackResource.getEntity().getName());
            LOGGER.info(buildpackResource.getEntity().getStack());
            Thread.sleep(4000);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            CompletableFuture.runAsync(() -> {
                // build 생성
                CreateBuildResponse createdBuild = createBuild(packageGuid, buildpackResource.getEntity());

                String buildId = createdBuild.getId();

                int count = 1;

                // 조회한 build 의 state 가 STAGED 가 아닌 경우 STAGED 될때까지 반복
                while(buildCompleted(buildId).equals(false)){
                    GetBuildResponse build = getBuild(buildId);
                    LOGGER.info("머리 아프다 집에 가자 이건 또 왜 안되니 ::::::::::::: " + build.toString());


                    if(buildCompleted(build.getId())){
                        LOGGER.info("이거슨 트루다 트루. 참 트루!!!!!!!!!!!");
                        assignDroplet(appGuid, getBuild(build.getId()).getDroplet().getId());
                        LOGGER.info("나간다 와일 문................");
                        break;
                    }

                    LOGGER.info("count ::: " + count);
                    count++;
                }



/*
                // 위에서 만든 app 을 build 한 후 build list 에 해당 app 이 있는지 확인.
                while(Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).builds().list(ListBuildsRequest.builder().applicationId(appGuid).build()).block().getResources().get(0).getDroplet() == null){
                    try {
                        LOGGER.info("::::::::::::::::::::::::::::::::::::::::::" + Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).builds().list(ListBuildsRequest.builder().applicationId(appGuid).build()).block().toString());
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 있으면 listBuildsResponse 조회!
                ListBuildsResponse listBuildsResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).builds()
                        .list(ListBuildsRequest.builder()
                                .applicationId(appGuid)
                                .build()).block();

                LOGGER.info("요고닷!!!!" + listBuildsResponse.toString());*/


//                try {
//                    Thread.sleep(4000);
//                    LOGGER.info("::::::::::::::::::::::::::::::::::");
//
//                    assignDroplet(appGuid, listBuildsResponse.getResources().get(0).getDroplet().getId());
//
//                } catch (Exception error) {
//                    LOGGER.info(error.getMessage());
//                }

                try {
                    // 라우트 생성
                    CreateRouteResponse route = createRoute(app);

                    // 라우트 매핑
                    routeMapping(appGuid, route);

                    // app 시작
                    StartApplicationResponse startApplicationResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).applicationsV3()
                            .start(StartApplicationRequest.builder()
                                    .applicationId(appGuid).build()).block();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            },executor);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

/*
        String buildGuid = null;

        // Build 생성한 후 buildGuid 조회
        while(buildGuid == null){
            try{
                buildGuid = createBuild(packageGuid).getId();
            }catch (NullPointerException e){
                System.out.println("뷜드 null 이야 임뫄~~~");
            }

            if(buildGuid != null){
                LOGGER.info("buildGuid = " + buildGuid);
                break;
            }
        }


        // droplet guid
        String dropletGuid = null;

        // buildGuid 로 해당 dropletGuid 조회
        while(dropletGuid == null){
            try{
                dropletGuid = getBuild(buildGuid).getDroplet().getId();
            }catch (NullPointerException e){
                System.out.println("드뢉뤳 null 이야 임뫄~~~");
            }

            if(dropletGuid != null){
                assignDroplet(appGuid, dropletGuid);
                break;
            }
        }*/
    }


    /**
     * Create an empty app (POST /v3/apps)
     *
     * @param app the app
     * @return CreateApplicationResponse
     */
    private CreateApplicationResponse createEmptyApp(App app){
        CreateApplicationResponse appResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).applicationsV3()
                .create(CreateApplicationRequest.builder()
                        .name(app.getName())
                        .relationships(ApplicationRelationships.builder()
                                .space(ToOneRelationship.builder()
                                        .data(Relationship.builder()
                                                .id(app.getSpaceGuid())
                                                .build())
                                        .build())
                                .build())
                        .build()).block();
        LOGGER.info("빈 app 생성~~");
        return appResponse;
    }


    /**
     * Create an empty package for the app (POST /v3/packages)
     *
     * @param appGuid the appGuid
     * @return CreatePackageResponse
     */
    private CreatePackageResponse createEmptyPackage(String appGuid){
        CreatePackageResponse packageResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).packages()
                .create(CreatePackageRequest.builder()
                        .type(PackageType.BITS)
                        .relationships(PackageRelationships.builder()
                                .application(ToOneRelationship.builder()
                                        .data(Relationship.builder()
                                                .id(appGuid)
                                                .build())
                                        .build())
                                .build())
                        .build()
                ).block();
        LOGGER.info("package 생성~~");
        return packageResponse;
    }


    /**
     * Upload package
     *
     * @param packageGuid the packageGuid
     */
    private void uploadPackage(String packageGuid){
        // If your package is type buildpack, upload your bits to your new package (POST /v3/packages/:guid/upload)
        UploadPackageResponse uploadPackageResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).packages()
                .upload(UploadPackageRequest.builder()
                        .packageId(packageGuid)
                        .bits(FileSystems.getDefault().getPath(localUploadPath,"spring-music.war"))
                        //.bits(new ClassPathResource(localUploadPath + "/php-sample.zip").getFile().toPath())
                        .build()).block();
        LOGGER.info("package 업로드~~");
    }


    /**
     * Stage your package and create a build (POST /v3/builds)
     *
     * @param packageGuid the packageGuid
     * @return CreateBuildResponse
     */
    private CreateBuildResponse createBuild(String packageGuid, BuildpackEntity buildpackEntity){
        CreateBuildResponse createBuildResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).builds()
                .create(CreateBuildRequest.builder()
                        .getPackage(Relationship.builder()
                                .id(packageGuid).build())
                        .lifecycle(Lifecycle.builder()
                                .type(LifecycleType.BUILDPACK)
                                .data(BuildpackData.builder()
                                        .buildpacks(buildpackEntity.getName())
                                        .stack(buildpackEntity.getStack())
                                        .build()).build())
                        //.build()).delaySubscription(Duration.ofSeconds(60)).block();
                        .build()).block();
        LOGGER.info("Build 생성~~");
        return createBuildResponse;
    }


    /**
     * Get the droplet corresponding to the staged build
     *
     * @param buildGuid the buildGuid
     * @return GetBuildResponse
     */
    private GetBuildResponse getBuild(String buildGuid) {
        GetBuildResponse getBuildResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).builds()
                .get(GetBuildRequest.builder()
                        .buildId(buildGuid)
                        //.build()).blockOptional(Duration.ofSeconds(60)).get();
                        .build()).block();

        LOGGER.info("droplet 조회~~");
        return getBuildResponse;
    }


    /**
     * Assign the droplet to the app (PATCH /v3/apps/:guid/relationships/current_droplet)
     *
     * @param appGuid the appGuid
     * @param dropletGuid the dropletGuid
     */
    private void assignDroplet(String appGuid, String dropletGuid) {
        LOGGER.info("appGuid :::: " + appGuid + " & dropletGuid :::: " + dropletGuid);
        SetApplicationCurrentDropletResponse currentDropletResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).applicationsV3()
                .setCurrentDroplet(SetApplicationCurrentDropletRequest.builder()
                        .applicationId(appGuid)
                        .data(Relationship.builder()
                                .id(dropletGuid).build())
                        .build()).block();
        LOGGER.info("droplet assign~~");
    }


    /**
     * Create a route
     *
     * @param app the app
     * @return CreateRouteResponse
     */
    private CreateRouteResponse createRoute(App app) {
        CreateRouteResponse createRouteResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).routes()
                .create(CreateRouteRequest.builder()
                        .host(app.getHostName())
                        .domainId(app.getDomainId())
                        .spaceId(app.getSpaceGuid())
                        .build()).block();
        LOGGER.info("라우트 생성~~");
        return createRouteResponse;
    }


    /**
     * Mapping the route to your app
     *
     * @param appGuid the appGuid
     * @param route the route
     */
    private void routeMapping(String appGuid, CreateRouteResponse route) {
        CreateRouteMappingResponse createRouteMappingResponse = Common.cloudFoundryClient(connectionContext(), tokenProvider(getToken())).routeMappings()
                .create(CreateRouteMappingRequest.builder()
                        .applicationId(appGuid)
                        .routeId(route.getMetadata().getId())
                        .build()).block();
    }


    /**
     * build 된 상태가 STAGED 인지 판별.
     *
     * @param buildGuid the buildGuid
     * @return Boolean
     */
    public Boolean buildCompleted (String buildGuid) {
        switch (getBuild(buildGuid).getState()) {
            case FAILED:
                return  false;
            case STAGED:
                return true;
            default:
                return false;
        }
    }
}
