/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Marcelo Guimarães
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.backpackcloud.sherlogholmes.domain.types;

import java.util.Objects;

public class SemanticVersion implements Comparable<SemanticVersion> {

  private final int major;
  private final int minor;
  private final int micro;

  public SemanticVersion(int major, int minor, int micro) {
    this.major = major;
    this.minor = minor;
    this.micro = micro;
  }

  @Override
  public String toString() {
    return String.format("%d.%d.%d", major, minor, micro);
  }

  public static SemanticVersion of(String value) {
    String[] fields = value.split("\\.");
    int major = Integer.parseInt(fields[0]);
    int minor = fields.length > 1 ? Integer.parseInt(fields[1]) : 0;
    int patch = fields.length > 2 ? Integer.parseInt(fields[2]) : 0;

    return new SemanticVersion(major, minor, patch);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SemanticVersion that = (SemanticVersion) o;
    return major == that.major && minor == that.minor && micro == that.micro;
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, micro);
  }

  @Override
  public int compareTo(SemanticVersion that) {
    if (this.major > that.major) {
      return 1;
    } else if (this.major < that.major) {
      return -1;
    }

    if (this.minor > that.minor) {
      return 1;
    } else if (this.minor < that.minor) {
      return -1;
    }

    if (this.micro > that.minor) {
      return 1;
    } else if (this.micro < that.micro) {
      return -1;
    }

    return 0;
  }

}
