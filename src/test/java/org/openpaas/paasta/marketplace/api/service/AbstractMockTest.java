package org.openpaas.paasta.marketplace.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openpaas.paasta.marketplace.api.domain.Category;
import org.openpaas.paasta.marketplace.api.domain.Instance;
import org.openpaas.paasta.marketplace.api.domain.Profile;
import org.openpaas.paasta.marketplace.api.domain.Software;
import org.openpaas.paasta.marketplace.api.domain.SoftwareHistory;
import org.openpaas.paasta.marketplace.api.domain.TestSoftwareInfo;
import org.openpaas.paasta.marketplace.api.domain.User;
import org.openpaas.paasta.marketplace.api.domain.Yn;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractMockTest {

    protected String userId;

    protected String adminId;

    protected LocalDateTime current;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        userId = "foo";
        adminId = "admin";
        current = LocalDateTime.now();
    }

    protected Category category(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName("category-01");
        category.setDescription("descritpion");
        category.setCreatedBy("admin");
        category.setCreatedDate(current);
        category.setLastModifiedBy("admin");
        category.setLastModifiedDate(current);
        category.setSeq(id);

        return category;
    }

    protected Software software(Long id, String name, Category category) {
        Software software = new Software();
        software.setId(id);
        software.setName(name);
        software.setStatus(Software.Status.Approval);
        software.setCategory(category);
        software.setSummary("category-01's summary.");
        software.setDescription("description of this software. create by " + userId);
        software.setCreatedBy(userId);
        software.setCreatedDate(current);
        software.setLastModifiedBy(userId);
        software.setLastModifiedDate(current);
        software.setApp(String.format("app-%s.jar", UUID.randomUUID()));
        software.setManifest(String.format("manifest-%s.yml", UUID.randomUUID()));
        software.setIcon(String.format("icon-%s.png", UUID.randomUUID()));
        List<String> screenshotList = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            screenshotList.add(String.format("screenshot-%s.jpg", UUID.randomUUID()));
        }
        software.setScreenshotList(screenshotList);
        software.setType(Software.Type.Web);
        software.setPricePerMonth(1000L);
        software.setVersion("1.0");
        software.setInUse(Yn.Y);

        return software;
    }

    protected SoftwareHistory softwareHistory(Long id, Software software, String description) {
        SoftwareHistory softwareHistory = new SoftwareHistory();
        softwareHistory.setId(1L);
        softwareHistory.setSoftware(software);
        softwareHistory.setDescription(description);

        softwareHistory.setCreatedBy(userId);
        softwareHistory.setCreatedDate(current);
        softwareHistory.setLastModifiedBy(userId);
        softwareHistory.setLastModifiedDate(current);
        software.setInUse(Yn.Y);

        return softwareHistory;
    }

    protected Instance instance(Long id, Software software) {
        Instance instance = new Instance();
        instance.setId(id);
        instance.setSoftware(software);
        instance.setStatus(Instance.Status.Approval);
        instance.setProvisionStatus(Instance.ProvisionStatus.Pending);
        instance.setUsageStartDate(current);
        instance.setCreatedBy(userId);
        instance.setCreatedDate(current);
        instance.setLastModifiedBy(userId);
        instance.setLastModifiedDate(current);
        instance.setInUse(Yn.Y);

        return instance;
    }

    protected User user(String id) {
        User user = new User();
        user.setId(id);
        user.setName("name-" + id);
        user.setRole(User.Role.User);
        user.setCreatedBy(id);
        user.setCreatedDate(current);
        user.setLastModifiedBy(id);
        user.setLastModifiedDate(current);
        user.setInUse(Yn.Y);

        return user;
    }

    protected Profile profile(String id) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setName("name-" + id);
        profile.setType(Profile.Type.Company);
        profile.setCreatedBy(id);
        profile.setCreatedDate(current);
        profile.setLastModifiedBy(id);
        profile.setLastModifiedDate(current);
        profile.setInUse(Yn.Y);

        return profile;
    }

    protected TestSoftwareInfo testSoftwareInfo(Long id, Long softwareId, Long planGuid) {
        TestSoftwareInfo testSoftwareInfo = new TestSoftwareInfo();
        testSoftwareInfo.setId(id);
        testSoftwareInfo.setName("name-" + id);
        testSoftwareInfo.setSoftwareId(softwareId);
        testSoftwareInfo.setAppGuid("APP_GUID-" + id);
        testSoftwareInfo.setPlanGuid(planGuid);
        testSoftwareInfo.setCreatedDate(current);

        return testSoftwareInfo;
    }

}
