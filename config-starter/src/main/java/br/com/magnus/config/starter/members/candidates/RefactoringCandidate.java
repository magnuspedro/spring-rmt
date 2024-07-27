package br.com.magnus.config.starter.members.candidates;

import br.com.magnus.config.starter.members.detectors.methods.Reference;
import br.com.magnus.config.starter.patterns.DesignPattern;

import java.util.List;

public interface RefactoringCandidate {

    String getId();

    Reference getReference();

    String getPkg();

    String getClassName();

    DesignPattern getEligiblePattern();

}
