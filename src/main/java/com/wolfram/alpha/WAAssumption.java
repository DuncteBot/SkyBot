/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created on Nov 8, 2009
 *
 */
package com.wolfram.alpha;


public interface WAAssumption {
    
    String TYPE_CLASH = "Clash";
    String TYPE_MULTICLASH = "MultiClash";
    String TYPE_UNIT = "Unit";
    String TYPE_ANGLEUNIT = "AngleUnit";
    String TYPE_FUNCTION = "Function";
    String TYPE_SUBCATEGORY = "SubCategory";
    String TYPE_ATTRIBUTE = "Attribute";
    String TYPE_TIMEAMORPM = "TimeAMOrPM";
    String TYPE_DATEORDER = "DateOrder";
    String TYPE_LISTORTIMES = "ListOrTimes";
    String TYPE_LISTORNUMBER = "ListOrNumber";
    String TYPE_COORDINATESYSTEM = "CoordinateSystem";
    String TYPE_I = "I";
    String TYPE_NUMBERBASE = "NumberBase";
    String TYPE_MIXEDFRACTION = "MixedFraction";
    String TYPE_MORTALITYYEARDOB = "MortalityYearDOB";
    String TYPE_DNAORSTRING = "DNAOrString";
    String TYPE_TIDESTATION = "TideStation";
    
    String TYPE_FORMULASELECT = "FormulaSelect";
    String TYPE_FORMULASOLVE = "FormulaSolve";
    String TYPE_FORMULAVARIABLE = "FormulaVariable";
    String TYPE_FORMULAVARIABLEOPTION = "FormulaVariableOption";
    String TYPE_FORMULAVARIABLEINCLUDE = "FormulaVariableInclude";
    
    
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
