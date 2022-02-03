package com.krish.empower.jdocs;

public class DiffInfo {

  private PathDiffResult diffResult;
  private PathValue left;
  private PathValue right;

  public DiffInfo(PathDiffResult diffResult, PathValue left, PathValue right) {
    this.diffResult = diffResult;
    this.left = left;
    this.right = right;
  }

  public PathDiffResult getDiffResult() {
    return diffResult;
  }

  public PathValue getLeft() {
    return left;
  }

  public PathValue getRight() {
    return right;
  }

}
