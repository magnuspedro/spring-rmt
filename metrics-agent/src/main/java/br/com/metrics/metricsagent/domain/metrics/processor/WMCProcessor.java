package br.com.metrics.metricsagent.domain.metrics.processor;

import br.com.metrics.metricsagent.domain.metrics.report.CKNumber;
import br.com.metrics.metricsagent.domain.metrics.report.CKReport;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.*;

public class WMCProcessor extends ASTVisitor implements MetricProcessor {

	protected int cc = 0;

	public boolean visit(MethodDeclaration node) {
		increaseCc();
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		increaseCc();
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		increaseCc();
		return super.visit(node);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		increaseCc();
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		increaseCc();
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		increaseCc();
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		if (!node.isDefault()) {
			increaseCc();
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		increaseCc();
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		increaseCc();
		return super.visit(node);
	}

	public boolean visit(IfStatement node) {
		increaseCc(countORsANDs(node));
		increaseCc();
		return super.visit(node);
	}
	
	private int countORsANDs(IfStatement node) {
		String expr = node.getExpression().toString().replace("&&", "&").replace("||", "|");
		return StringUtils.countMatches(expr, "&") +  StringUtils.countMatches(expr, "|");
	}

	private void increaseCc() {
		increaseCc(1);
	}

	protected void increaseCc(int qtd) {
		cc += qtd;
	}

	@Override
	public void execute(CompilationUnit cu, CKNumber number, CKReport report) {
		cu.accept(this);

	}

	@Override
	public void setResult(CKNumber result) {
		result.setWmc(cc);
	}

}
