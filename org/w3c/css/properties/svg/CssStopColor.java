//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.svg;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec hhttp://www.w3.org/TR/2011/REC-SVG11-20110816/pservers.html#StopColorProperty
 * @see CssFill parseIccColor
 */
public class CssStopColor extends org.w3c.css.properties.css.CssStopColor {

    public static final CssIdent currentColor = CssIdent.getIdent("currentColor");

    /**
     * Create a new CssStopColor
     */
    public CssStopColor() {
        value = initial;
    }

    /**
     * Creates a new CssStopColor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssStopColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = parseColor(ac, expression, check, this);
    }

    public CssStopColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    static CssValue parseColor(ApplContext ac, CssExpression expression,
                               boolean check, CssProperty property)
            throws InvalidParamException {

        property.setByUser();

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue value = null;
        CssValue val;
        char op;
        ArrayList<CssValue> values = new ArrayList<>();
        boolean gotColor = false;
        boolean gotIccColor = false;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        value = inherit;
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    property.getPropertyName(), ac);
                        }
                    }
                    if (currentColor.equals(val)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    property.getPropertyName(), ac);
                        }
                        values.add(currentColor);
                    }
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                property.getPropertyName(), ac);
                    }
                    // else it must be a color
                    if (gotColor) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                property.getPropertyName(), ac);
                    }
                    CssExpression nex = new CssExpression();
                    nex.addValue(val);
                    switch (ac.getCssVersion()) {
                        case CSS1:
                            // should never happen...
                            throw new InvalidParamException("unrecognize", ac);
                        case CSS2:
                        case CSS21:
                            values.add((new org.w3c.css.properties.css21.CssColor(ac, nex, check)).getColor());
                            break;
                        case CSS3:
                        case CSS_2015:
                        case CSS:
                        default:
                            values.add((new org.w3c.css.properties.css3.CssColor(ac, nex, check)).getColor());
                            break;
                    }
                    gotColor = true;
                    break;
                case CssTypes.CSS_HASH_IDENT:
                    if (gotColor) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                property.getPropertyName(), ac);
                    }
                    CssColor c = new CssColor();
                    c.setShortRGBColor(ac, val.toString());
                    gotColor = true;
                    values.add(c);
                    break;
                case CssTypes.CSS_COLOR:
                    if (gotColor) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                property.getPropertyName(), ac);
                    }
                    gotColor = true;
                    values.add(val);
                    break;
                case CssTypes.CSS_FUNCTION:
                    CssFunction f = (CssFunction) val;
                    if (gotColor || gotIccColor) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                property.getPropertyName(), ac);
                    }
                    if ("icc-color".equals(f.getName())) {
                        values.add(CssFill.parseIccColor(ac, f, check));
                        gotIccColor = true;
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            property.getPropertyName(), ac);
            }
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        if (!values.isEmpty()) {
            value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
        }
        return value;
    }

}

