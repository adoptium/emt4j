package org.eclipse.emt4j.common.rule.impl;

import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.ClassURL;
import org.eclipse.emt4j.common.util.FileUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RuleImpl(type = "deprecated-api")
public class DeprecatedAPIRule extends ExecutableRule {
    private String classListFile;
    private Set<String> deprecatedClasses = new HashSet<>();
    private Set<DependTarget.Method> deprecatedMethods = new HashSet<>();

    public DeprecatedAPIRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + classListFile, false).forEach((l) -> {
            String[] nameMethodDesc = l.split(",");
            if (DependTarget.Method.ANY_DESC.equals(nameMethodDesc[1])) {
                deprecatedClasses.add(nameMethodDesc[0]);
            } else {
                deprecatedMethods.add(new DependTarget.Method(nameMethodDesc[0], nameMethodDesc[1], nameMethodDesc[2], DependType.METHOD));
            }
        });
    }

    @Override
    protected CheckResult check(Dependency dependency) {
        Set<DependTarget.Method> methods = dependency.getClassSymbol().getCallMethodSet()
                .stream()
                .filter((m) -> deprecatedClasses.contains(m.getClassName()) || deprecatedMethods.contains(m)).collect(Collectors.toSet());
        if (methods.isEmpty()) {
            return CheckResult.PASS;
        } else {
            CheckResult result = CheckResult.fail();
            result.setPropagated(methods.stream().map((m) ->
                    new Dependency(ClassURL.create(dependency.getLocationExternalForm(), dependency.getTarget().asClass().getClassName(), ""),
                            m, null,dependency.getTargetFilePath())
            ).collect(Collectors.toList()));
            return result;
        }
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.WHOLE_CLASS == dependency.getDependType();
    }

    public void setClassListFile(String classListFile) {
        this.classListFile = classListFile;
    }
}
