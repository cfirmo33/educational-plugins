package com.jetbrains.edu.java;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.project.Project;
import com.intellij.platform.DirectoryProjectGenerator;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.newproject.EduCourseProjectGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class EduJavaCourseProjectGenerator implements EduCourseProjectGenerator {
  private Course myCourse;

  @NotNull
  @Override
  public DirectoryProjectGenerator getDirectoryProjectGenerator() {
    return new EduJavaDirectoryProjectGenerator(myCourse);
  }

  @Nullable
  @Override
  public Object getProjectSettings() {
    return new Object();
  }

  @Override
  public void setCourse(@NotNull Course course) {
    myCourse = course;
  }

  @Override
  public ValidationResult validate() {
    return ValidationResult.OK;
  }

  @Override
  public boolean beforeProjectGenerated() {
    return true;
  }

  @Override
  public void afterProjectGenerated(@NotNull Project project) {
  }
}