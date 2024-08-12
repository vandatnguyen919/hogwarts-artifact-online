package edu.tcu.cs.hogwartsartifactsonline.wizard;

import edu.tcu.cs.hogwartsartifactsonline.artifact.Artifact;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Entity
public class Wizard implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE},mappedBy = "owner")
    private List<Artifact> artifacts = new ArrayList<>();

    public void addArtifact(Artifact artifact) {
        artifact.setOwner(this);
        artifacts.add(artifact);
    }

    public void removeAllArtifacts() {
        artifacts.forEach(artifact -> artifact.setOwner(null));
        artifacts.clear();
    }

    public Integer getNumberOfArtifacts() {
        return this.artifacts.size();
    }
}
