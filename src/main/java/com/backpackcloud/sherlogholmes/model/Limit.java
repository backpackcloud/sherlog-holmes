package com.backpackcloud.sherlogholmes.model;

import com.backpackcloud.text.InputValue;

import java.time.temporal.ChronoUnit;

public class Limit {

  private final int amount;
  private final ChronoUnit unit;
  private final Type type;
  private final Position position;

  public Limit(int amount, ChronoUnit unit, Position position) {
    this.amount = amount;
    this.unit = unit;
    this.type = unit == null ? Type.OCCURRENCE : Type.TIME;
    this.position = position;
  }

  public Limit(int amount, Position position) {
    this.amount = amount;
    this.unit = null;
    this.type = Type.OCCURRENCE;
    this.position = position;
  }

  public int amount() {
    return amount;
  }

  public ChronoUnit unit() {
    return unit;
  }

  public Type type() {
    return type;
  }

  public Position position() {
    return position;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (position == Position.HEAD) {
      result.append("first ");
    } else {
      result.append("last ");
    }
    result.append(amount);
    if (unit != null) {
      result.append(" ").append(InputValue.of(unit).get());
    }
    return result.toString();
  }

  public enum Type {
    OCCURRENCE, TIME
  }

  public enum Position {
    HEAD, TAIL
  }

}
