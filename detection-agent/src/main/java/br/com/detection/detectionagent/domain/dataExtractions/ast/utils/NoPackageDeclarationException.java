package br.com.detection.detectionagent.domain.dataExtractions.ast.utils;

import br.com.detection.detectionagent.domain.dataExtractions.ast.utils.exceptions.AstHandlerException;

public class NoPackageDeclarationException extends AstHandlerException {
    public NoPackageDeclarationException() {
        super("Package declaration not be found");
    }
}
