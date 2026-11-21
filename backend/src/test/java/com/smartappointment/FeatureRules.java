package com.smartappointment;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Optional;

/** Custom ArchUnit conditions for feature boundaries (ADR-0001). */
final class FeatureRules {

  private static final String ROOT = "com.smartappointment.";
  private static final String COMMON = "common";

  private FeatureRules() {}

  static ArchCondition<JavaClass> notUseRepositoriesOfOtherFeatures() {
    return new ArchCondition<>("not use repositories of other features") {
      @Override
      public void check(JavaClass origin, ConditionEvents events) {
        Optional<String> originFeature = featureOf(origin);
        for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
          JavaClass target = dependency.getTargetClass();
          if (isRepositoryOfOtherFeature(originFeature, target)) {
            events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
          }
        }
      }
    };
  }

  private static boolean isRepositoryOfOtherFeature(
      Optional<String> originFeature, JavaClass target) {
    Optional<String> targetFeature = featureOf(target);
    return target.getSimpleName().endsWith("Repository")
        && targetFeature.isPresent()
        && !targetFeature.equals(originFeature);
  }

  /** The first package segment under com.smartappointment, excluding common. */
  static Optional<String> featureOf(JavaClass type) {
    String packageName = type.getPackageName();
    if (!packageName.startsWith(ROOT)) {
      return Optional.empty();
    }
    String feature = packageName.substring(ROOT.length()).split("\\.")[0];
    return COMMON.equals(feature) ? Optional.empty() : Optional.of(feature);
  }
}
