package com.shaunyl.enver.command.support;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 *
 * @author Filippo
 */
public class CharBooleanValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value)
            throws ParameterException {
        if (!"n".equals(value) && !"y".equals(value)) {
            throw new ParameterException("Parameter " + name + " should be \"n\" or \"y\" (found " + value + ")");
        }
    }
}
