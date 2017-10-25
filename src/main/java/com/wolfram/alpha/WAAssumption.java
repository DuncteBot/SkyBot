/*
 * Created on Nov 8, 2009
 *
 */
package com.wolfram.alpha;


public interface WAAssumption {

    static final String TYPE_CLASH = "Clash";
    static final String TYPE_MULTICLASH = "MultiClash";
    static final String TYPE_UNIT = "Unit";
    static final String TYPE_ANGLEUNIT = "AngleUnit";
    static final String TYPE_FUNCTION = "Function";
    static final String TYPE_SUBCATEGORY = "SubCategory";
    static final String TYPE_ATTRIBUTE = "Attribute";
    static final String TYPE_TIMEAMORPM = "TimeAMOrPM";
    static final String TYPE_DATEORDER = "DateOrder";
    static final String TYPE_LISTORTIMES = "ListOrTimes";
    static final String TYPE_LISTORNUMBER = "ListOrNumber";
    static final String TYPE_COORDINATESYSTEM = "CoordinateSystem";
    static final String TYPE_I = "I";
    static final String TYPE_NUMBERBASE = "NumberBase";
    static final String TYPE_MIXEDFRACTION = "MixedFraction";
    static final String TYPE_MORTALITYYEARDOB = "MortalityYearDOB";
    static final String TYPE_DNAORSTRING = "DNAOrString";
    static final String TYPE_TIDESTATION = "TideStation";
    
    static final String TYPE_FORMULASELECT = "FormulaSelect";
    static final String TYPE_FORMULASOLVE = "FormulaSolve";
    static final String TYPE_FORMULAVARIABLE = "FormulaVariable";
    static final String TYPE_FORMULAVARIABLEOPTION = "FormulaVariableOption";
    static final String TYPE_FORMULAVARIABLEINCLUDE = "FormulaVariableInclude";
    
    
    String getType();
    int getCount();
    String getWord();
    String getDescription();
    int getCurrent();
    
    String[] getNames();
    String[] getDescriptions();
    String[] getInputs();
    String[] getWords();
    boolean[] getValidities();
    
}
