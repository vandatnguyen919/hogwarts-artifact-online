package edu.tcu.cs.hogwartsartifactsonline.wizard;

import edu.tcu.cs.hogwartsartifactsonline.system.Result;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.wizard.converter.WizardDtoToWizardConverter;
import edu.tcu.cs.hogwartsartifactsonline.wizard.converter.WizardToWizardDtoConverter;
import edu.tcu.cs.hogwartsartifactsonline.wizard.dto.WizardDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/wizards")
public class WizardController {

    private final WizardService wizardService;

    private final WizardToWizardDtoConverter wizardToWizardDtoConverter;

    private final WizardDtoToWizardConverter wizardDtoToWizardConverter;

    public WizardController(WizardService wizardService, WizardToWizardDtoConverter wizardToWizardDtoConverter, WizardDtoToWizardConverter wizardDtoToWizardConverter) {
        this.wizardService = wizardService;
        this.wizardToWizardDtoConverter = wizardToWizardDtoConverter;
        this.wizardDtoToWizardConverter = wizardDtoToWizardConverter;
    }

    @GetMapping("/{wizardId}")
    public Result findWizardById(@PathVariable Integer wizardId) {
        Wizard wizard = wizardService.findById(wizardId);
        WizardDto wizardDto = wizardToWizardDtoConverter.convert(wizard);
        return new Result(true, StatusCode.SUCCESS, "Find One Success", wizardDto);
    }

    @GetMapping
    public Result findAllWizards() {
        List<Wizard> wizards = wizardService.findAll();
        List<WizardDto> wizardDtos = wizards.stream().map(wizardToWizardDtoConverter::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Find All Success", wizardDtos);
    }

    @PostMapping
    public Result addWizard(@Valid @RequestBody WizardDto wizardDto) {
        Wizard wizard = wizardDtoToWizardConverter.convert(wizardDto);
        Wizard savedWizard = wizardService.save(wizard);
        WizardDto savedWizardDto = wizardToWizardDtoConverter.convert(savedWizard);
        return new Result(true, StatusCode.SUCCESS, "Add Success", savedWizardDto);
    }

    @PutMapping("/{wizardId}")
    public Result updateWizard(@PathVariable Integer wizardId, @Valid @RequestBody WizardDto wizardDto) {
        Wizard update = wizardDtoToWizardConverter.convert(wizardDto);
        Wizard updatedWizard = wizardService.update(wizardId, update);
        WizardDto updatedWizardDto = wizardToWizardDtoConverter.convert(updatedWizard);
        return new Result(true, StatusCode.SUCCESS, "Update Success", updatedWizardDto);
    }

    @DeleteMapping("/{wizardId}")
    public Result deleteWizard(@PathVariable Integer wizardId) {
        wizardService.delete(wizardId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }
}
