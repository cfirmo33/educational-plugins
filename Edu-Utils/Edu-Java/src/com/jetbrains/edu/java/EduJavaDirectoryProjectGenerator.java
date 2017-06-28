package com.jetbrains.edu.java;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.ide.util.newProjectWizard.AbstractProjectWizard;
import com.intellij.ide.util.newProjectWizard.StepSequence;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.LanguageLevelProjectExtensionImpl;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.actions.NewModuleAction;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.util.SystemIndependent;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.intellij.generation.EduCourseModuleBuilder;
import com.jetbrains.edu.learning.intellij.generation.EduProjectGenerator;
import com.jetbrains.edu.utils.generation.EduModuleBuilderUtils;
import icons.EducationalCoreIcons;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;


public class EduJavaDirectoryProjectGenerator implements DirectoryProjectGenerator {
  private final Course myCourse;

  EduJavaDirectoryProjectGenerator(Course course) {
    super();
    myCourse = course;
  }

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "Java Project Generator";
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return EducationalCoreIcons.EducationalProjectType;
  }

  @Override
  public void generateProject(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull Object o, @NotNull Module module) {
    configureProject(project);
    createCourseStructure(project);
  }

  private void configureProject(@NotNull Project project) {
    setSdk(project);
    setCompilerOutput(project);
  }

  private void setSdk(@NotNull Project project) {
    final ProjectSdksModel projectJdksModel = ProjectStructureConfigurable.getInstance(project).getProjectJdksModel();
    if (!projectJdksModel.isInitialized()) { //should be initialized
      projectJdksModel.reset(project);
    }
    final Collection<Sdk> projectSdks = projectJdksModel.getProjectSdks().values();
    for (Sdk sdk : projectSdks) {
      JavaSdkVersion version = JavaSdk.getInstance().getVersion(sdk);
      if (version != null && version.isAtLeast(JavaSdkVersion.JDK_1_8)) {
        ApplicationManager.getApplication().runWriteAction(() -> ProjectRootManager.getInstance(project).setProjectSdk(sdk));
        LanguageLevel languageLevel = JavaSdkVersion.JDK_1_8.getMaxLanguageLevel();
        LanguageLevelProjectExtensionImpl.getInstanceImpl(project).setCurrentLevel(languageLevel);
      }
    }
  }

  private void setCompilerOutput(@NotNull Project project) {
    CompilerProjectExtension compilerProjectExtension = CompilerProjectExtension.getInstance(project);
    @SystemIndependent String basePath = project.getBasePath();
    if (compilerProjectExtension != null && basePath != null) {
      compilerProjectExtension.setCompilerOutputUrl(VfsUtilCore.pathToUrl(basePath));
    }
  }

  private void createCourseStructure(@NotNull Project project) {
    new NewModuleAction().createModuleFromWizard(project, null, createProjectWizard(project));
  }

  @NotNull
  private AbstractProjectWizard createProjectWizard(@NotNull Project project) {
    return new AbstractProjectWizard("", project, project.getBaseDir().getPath()) {
      @Override
      public StepSequence getSequence() {
        return null;
      }

      @Override
      public ProjectBuilder getProjectBuilder() {
        return new EduCourseModuleBuilder() {
          @NotNull
          @Override
          public Module createModule(@NotNull ModifiableModuleModel moduleModel) throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException {
            Module baseModule = super.createModule(moduleModel);
            Project project = baseModule.getProject();
            EduProjectGenerator generator = new EduProjectGenerator();
            generator.setSelectedCourse(myCourse);
            EduModuleBuilderUtils.createCourseFromCourseInfo(moduleModel, project, generator, myCourse, getModuleFileDirectory());
            return baseModule;
          }
        };
      }
    };
  }

  @NotNull
  @Override
  public ValidationResult validate(@NotNull String s) {
    return ValidationResult.OK;
  }
}