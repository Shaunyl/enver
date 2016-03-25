/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shaunyl.enver.command.support;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Shaunyl
 */
public class PipeParameterSplitter {
    public List<String> split(String value) {
        return Arrays.asList(value.split("|"));
    }
}
