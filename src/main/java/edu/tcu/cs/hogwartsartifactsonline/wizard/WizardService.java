package edu.tcu.cs.hogwartsartifactsonline.wizard;

import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class WizardService {

    private final WizardRepository wizardRepository;

    public WizardService(WizardRepository wizardRepository) {
        this.wizardRepository = wizardRepository;
    }

    public Wizard findById(Integer wizardId) {
        return wizardRepository.findById(wizardId).orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
    }

    public List<Wizard> findAll() {
        return wizardRepository.findAll();
    }

    public Wizard save(Wizard wizard) {
        return wizardRepository.save(wizard);
    }

    public Wizard update(Integer wizardId, Wizard update) {
        return wizardRepository.findById(wizardId)
                .map(oldWizard -> {
                    oldWizard.setName(update.getName());
                    return wizardRepository.save(oldWizard);
                })
                .orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
    }

    public void delete(Integer wizardId) {
        Wizard wizard = wizardRepository.findById(wizardId).orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

        // unassign all artifacts from the wizard to be deleted
        wizard.removeAllArtifacts();
        wizardRepository.deleteById(wizardId);
    }
}
