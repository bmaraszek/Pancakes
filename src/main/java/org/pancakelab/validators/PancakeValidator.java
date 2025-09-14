package org.pancakelab.validators;

import org.pancakelab.model.Pancake;

public class PancakeValidator implements Validator<Pancake> {
  @Override
  public void validate(Pancake in) {
    if(in.getIngredients().size() > 5) {
      throw new IllegalStateException("Pancake can't have more than 5 ingredients");
    }
  }
}
