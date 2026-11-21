package com.smartappointment;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import jakarta.persistence.Entity;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/** Architecture rules from ADR-0001, ADR-0007 and ADR-0008. */
@AnalyzeClasses(
    packages = "com.smartappointment",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final DescribedPredicate<JavaClass> ENTITY =
      DescribedPredicate.describe("an entity", type -> type.isAnnotatedWith(Entity.class));

  @ArchTest
  void featuresHaveNoCycles(JavaClasses classes) {
    slices().matching("com.smartappointment.(*)..").should().beFreeOfCycles().check(classes);
  }

  @ArchTest
  void featuresUseOnlyTheirOwnRepositories(JavaClasses classes) {
    classes()
        .should(FeatureRules.notUseRepositoriesOfOtherFeatures())
        .allowEmptyShould(true)
        .check(classes);
  }

  @ArchTest
  void controllersDoNotUseRepositories(JavaClasses classes) {
    noClasses()
        .that()
        .areAnnotatedWith(RestController.class)
        .should()
        .dependOnClassesThat()
        .haveSimpleNameEndingWith("Repository")
        .allowEmptyShould(true)
        .check(classes);
  }

  @ArchTest
  void controllersDoNotReturnEntities(JavaClasses classes) {
    methods()
        .that()
        .areDeclaredInClassesThat()
        .areAnnotatedWith(RestController.class)
        .should()
        .notHaveRawReturnType(ENTITY)
        .allowEmptyShould(true)
        .check(classes);
  }

  @ArchTest
  void estimationIsPlainJava(JavaClasses classes) {
    classes()
        .that()
        .resideInAPackage("..estimation..")
        .should()
        .onlyDependOnClassesThat()
        .resideInAnyPackage("..estimation..", "java..")
        .allowEmptyShould(true)
        .check(classes);
  }

  @ArchTest
  void noFieldInjection(JavaClasses classes) {
    noFields().should().beAnnotatedWith(Autowired.class).allowEmptyShould(true).check(classes);
  }

  @ArchTest
  void filtersLiveInCommon(JavaClasses classes) {
    classes()
        .that()
        .implement(Filter.class)
        .should()
        .resideInAnyPackage("..common.web..", "..common.ratelimit..", "..common.security..")
        .allowEmptyShould(true)
        .check(classes);
  }

  @ArchTest
  void onlyConfigReadsEnvironment(JavaClasses classes) {
    noClasses()
        .that()
        .resideOutsideOfPackage("..common.config..")
        .should()
        .callMethod(System.class, "getenv")
        .orShould()
        .callMethod(System.class, "getenv", String.class)
        .check(classes);
  }
}
