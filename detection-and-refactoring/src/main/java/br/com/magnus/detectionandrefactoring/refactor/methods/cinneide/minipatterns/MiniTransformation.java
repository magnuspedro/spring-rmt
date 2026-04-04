package br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.minipatterns;

import br.com.magnus.detectionandrefactoring.refactor.methods.cinneide.CinneideContext;

/**
 * Interface for mini transformation patterns in refactoring.
 * <p>
 * Mini transformations are small, localized code changes that
 * support larger refactoring operations.
 */
public interface MiniTransformation {

    /**
     * Applies the transformation to the given context.
     *
     * @param context the refactoring context
     */
    void apply(CinneideContext context);
}
