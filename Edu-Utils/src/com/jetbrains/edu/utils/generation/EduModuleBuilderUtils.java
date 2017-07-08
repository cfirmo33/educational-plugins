package com.jetbrains.edu.utils.generation;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbModePermission;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.StudyUtils;
import com.jetbrains.edu.learning.core.EduNames;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseFormat.Lesson;
import com.jetbrains.edu.learning.courseFormat.TaskFile;
import com.jetbrains.edu.learning.courseFormat.tasks.Task;
import com.jetbrains.edu.learning.courseGeneration.StudyProjectGenerator;
import com.jetbrains.edu.utils.EduIntellijUtils;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class EduModuleBuilderUtils {

  private static final Logger LOG = Logger.getInstance(EduModuleBuilderUtils.class);

  private EduModuleBuilderUtils() {
  }

  public static void createCourseFromCourseInfo(@NotNull ModifiableModuleModel moduleModel,
                                         @NotNull Project project,
                                         @NotNull StudyProjectGenerator generator,
                                         @NotNull Course course,
                                         @Nullable String moduleDir) throws JDOMException, ModuleWithNameAlreadyExists, ConfigurationException, IOException {
    generator.setSelectedCourse(course);
    generator.generateProject(project, project.getBaseDir());
    updateAdaptiveCourseTaskFileNames(project, course);

    course = StudyTaskManager.getInstance(project).getCourse();
    if (course == null) {
      LOG.info("failed to generate course");
      return;
    }
    createCourseModuleContent(moduleModel, project, course, moduleDir);
  }

  public static void createCourseModuleContent(@NotNull ModifiableModuleModel moduleModel, Project project, Course course, String moduleDir)
    throws IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException {
    if (moduleDir == null) {
      return;
    }

    final Lesson additionalMaterials = course.getLessons(true).stream().
        filter(lesson -> EduNames.PYCHARM_ADDITIONAL.equals(lesson.getName())).findFirst().orElse(null);

    EduUtilModuleBuilder utilModuleBuilder = new EduUtilModuleBuilder(moduleDir, additionalMaterials);
    Module utilModule = utilModuleBuilder.createModule(moduleModel);

    createLessonModules(moduleModel, course, moduleDir, utilModule);

    ApplicationManager.getApplication().invokeLater(() -> DumbService.allowStartingDumbModeInside(DumbModePermission.MAY_START_BACKGROUND,
      () -> ApplicationManager.getApplication().runWriteAction(() -> StudyUtils.registerStudyToolWindow(course, project))));
  }


  private static void createLessonModules(@NotNull ModifiableModuleModel moduleModel, Course course, String moduleDir, Module utilModule)
    throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException {
    List<Lesson> lessons = course.getLessons();
    for (int i = 0; i < lessons.size(); i++) {
      int lessonVisibleIndex = i + 1;
      Lesson lesson = lessons.get(i);
      lesson.setIndex(lessonVisibleIndex);
      EduLessonModuleBuilder eduLessonModuleBuilder = new EduLessonModuleBuilder(moduleDir, lesson, utilModule);
      eduLessonModuleBuilder.createModule(moduleModel);
    }
  }

  private static void updateAdaptiveCourseTaskFileNames(@NotNull Project project, @NotNull Course course) {
    if (course.isAdaptive()) {
      Lesson adaptiveLesson = course.getLessons().get(0);
      Task task = adaptiveLesson.getTaskList().get(0);
      for (TaskFile taskFile : task.getTaskFiles().values()) {
        EduIntellijUtils.nameTaskFileAfterContainingClass(task, taskFile, project);
      }
    }
  }
}
