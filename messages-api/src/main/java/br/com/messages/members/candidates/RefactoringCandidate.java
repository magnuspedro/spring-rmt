package br.com.messages.members.candidates;

import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.patterns.DesignPattern;

public interface RefactoringCandidate<T> {

    String getId();

    Reference getReference();

    String getPkg();

    String getClassName();

    DesignPattern getEligiblePattern();

    boolean supports();

}
