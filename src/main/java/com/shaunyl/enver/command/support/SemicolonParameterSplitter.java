/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shaunyl.enver.command.support;

import com.beust.jcommander.converters.IParameterSplitter;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Filippo
 */
public class SemicolonParameterSplitter implements IParameterSplitter {

    public List<String> split(String value) {
        return Arrays.asList(value.split(";"));
    }
}
