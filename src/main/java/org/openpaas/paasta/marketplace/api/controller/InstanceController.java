package org.openpaas.paasta.marketplace.api.controller;

import lombok.RequiredArgsConstructor;
import org.openpaas.paasta.marketplace.api.domain.Instance;
import org.openpaas.paasta.marketplace.api.domain.InstanceSpecification;
import org.openpaas.paasta.marketplace.api.domain.Software;
import org.openpaas.paasta.marketplace.api.service.InstanceService;
import org.openpaas.paasta.marketplace.api.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(value = "/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final InstanceService instanceService;

    @GetMapping("/page")
    public Page<Instance> getPage(InstanceSpecification spec, Pageable pageable) {
        return instanceService.getPage(spec, pageable);
    }

    @GetMapping("/my/page")
    public Page<Instance> getMyPage(InstanceSpecification spec, Pageable pageable) {
        spec.setCreatedBy(SecurityUtils.getUserId());
        spec.setStatus(Instance.Status.Approval);
        return instanceService.getPage(spec, pageable);
    }

    @GetMapping("/my/totalPage")
    public Page<Instance> getMyTotalPage(InstanceSpecification spec, Pageable pageable) {
        spec.setCreatedBy(SecurityUtils.getUserId());
        return instanceService.getPage(spec, pageable);
    }

//    @GetMapping("/my/page/month")
//    public Page<Instance> getMyMonth(@RequestParam LocalDateTime usageStartDate, @RequestParam LocalDateTime usageEndDate, Pageable pageable) {
//        //spec.setCreatedBy(SecurityUtils.getUserId());
//        return instanceService.getPage2(SecurityUtils.getUserId(), usageStartDate, usageEndDate, pageable);
//    }

    @GetMapping("/{id}")
    public Instance get(@NotNull @PathVariable Long id) {
        return instanceService.get(id);
    }

    @PostMapping
    public Instance create(@NotNull @Validated @RequestBody Instance instance, BindingResult bindingResult)
            throws BindException {
        Software software = instance.getSoftware();
        if (software.getId() == null) {
            bindingResult.rejectValue("software.id", "Required");
        }

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        return instanceService.create(instance);
    }

    @DeleteMapping("/{id}")
    public Instance delete(@PathVariable @NotNull Long id) throws BindException {
        Instance saved = instanceService.get(id);
        SecurityUtils.assertCreator(saved);

        instanceService.updateToDeleted(id);

        return saved;
    }

}
