package edu.stanford.smi.protegex.owl.ui.widget;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.widget.NumberFieldWidget;
import edu.stanford.smi.protegex.owl.model.*;
import edu.stanford.smi.protegex.owl.util.InstanceUtil;

import javax.swing.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.awt.*;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Hemed Ali Al Ruwehy (hemed.ruwehy@uib.no)
 * @since 2015-11-04
 * Modified: 2017-09-18
 *
 * The University of Bergen Library
 *
 * <p>
 * A Protege OWL Slot Widget for validating xsd:gYear. The widget will be shown to any slot whenever
 * SingleLiteralWidget is applicable.
 *
 * The original SingleLiteralWidget with gYear datatype does not provide validation for gYear values,
 * hence we decided to develop our own gYear widget so that gYear values are well validated before being
 * assigned to the slot.
 */

public class UBBgYearWidget extends NumberFieldWidget {

    private static final Logger logger = Logger.getLogger(UBBgYearWidget.class.getName());
    private static final String INVALID_INPUT_MSG = "Invalid input";
    private static final String YEAR_FORMAT = " (yyyy)";
    private static final String INVALID_YEAR_MSG = "Invalid value for gYear: ";
    private static final String EMPTY_STRING = "";


    //A place where SingleLiteralWidget is applicable, this also is applicable
    public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
        return SingleLiteralWidget.isSuitable(cls, slot, facet);
    }


    @Override
    public void initialize() {
        super.initialize();
        this.setPreferredColumns(2);
    }

    @Override
    public String getLabel() {
        return super.getLabel().concat(YEAR_FORMAT);
    }


    /**
     * A method to validate xsd:gYear based on the input string.
     *
     * @param yearString a gYear string to parse
     *
     * @return {@code empty string} if a given string cannot be parsed to gYear, otherwise a valid gYear string
     */
    private String getValidXSDgYear(String yearString) {
        String xmlGYear;
        try {
            int inputYear = Integer.parseInt(yearString);
            /*
             * Input year should be at most 4 digits number. I had to make sure about this because if input year
             * is greater than 4 digits, it will be truncated by toXMLFormat() method anyway.
             **/
            if (inputYear > 0 && String.valueOf(inputYear).length() > 4) {
                return EMPTY_STRING;
            }
            //Negative gYear with at most 4 digits is allowed. e.g -0160 for 160BC
            if (inputYear < 0 && String.valueOf(inputYear).length() > 5) {
                return EMPTY_STRING;
            }

            XMLGregorianCalendar gCalendar = DatatypeFactory
                    .newInstance()
                    .newXMLGregorianCalendarDate(
                            inputYear,
                            DatatypeConstants.FIELD_UNDEFINED,
                            DatatypeConstants.FIELD_UNDEFINED,
                            DatatypeConstants.FIELD_UNDEFINED
                    );
            xmlGYear = gCalendar.toXMLFormat();

        } catch (DatatypeConfigurationException ex) {
            logger.log(Level.SEVERE, ex.getLocalizedMessage());
            xmlGYear = EMPTY_STRING;
        } catch (NumberFormatException nfe) {
            logger.log(Level.SEVERE, "gYear must be a number: ", nfe.getLocalizedMessage());
            xmlGYear = EMPTY_STRING;
        } catch (IllegalArgumentException ie) {
            logger.log(Level.SEVERE, ie.getLocalizedMessage());
            xmlGYear = EMPTY_STRING;
        }
        return xmlGYear;
    }


    /**
     * This method is called on the value change. The idea is to validate the input value for this slot
     * whenever there is a change in value.
     */

    @Override
    public Collection getValues() {
        String slotValueWithoutDatatype = InstanceUtil.stripDatatype(getText());
        String gYear = getValidXSDgYear(slotValueWithoutDatatype);
        RDFSLiteral gYearLiteral = null;

        if (slotValueWithoutDatatype != null && gYear.equals(EMPTY_STRING)) {
            invalidateSlot();
            logger.log(Level.SEVERE, "Invalid input for gYear: [" + slotValueWithoutDatatype + "]");
            //Display message to the user
            JOptionPane.showMessageDialog(
                    null, "Invalid value \"" + slotValueWithoutDatatype + "\" for gYear",
                    INVALID_INPUT_MSG,
                    JOptionPane.ERROR_MESSAGE
            );
        }
        if (!gYear.equals(EMPTY_STRING)) {
            //Create xsd:gYear literal
            gYearLiteral = getOWLModel().createRDFSLiteral(gYear, getXSDgYear());
        }
        assignPropertyValueToInstance(gYearLiteral);
        return CollectionUtilities.createCollection(gYearLiteral);
    }


    @Override
    public void setText(String s) {
        String plainText = InstanceUtil.stripDatatype(s);
        super.setText(plainText);
    }


    /**
     * Assign slot value to a corresponding individual, if it does not exist.
     */
    private void assignPropertyValueToInstance(Object newValue) {
        if (newValue != null) {
            Collection oldValues = getSubject().getPropertyValues(getPredicate());
            if (!oldValues.contains(newValue)) {
                getSubject().setPropertyValue(getPredicate(), newValue);
            }
        }
    }


    @Override
    public void setInvalidValueBorder() {
        //Do nothing
    }


    /**
     * Get RDF resource of this slot
     */
    private RDFResource getSubject() {
        return (RDFResource) getInstance();
    }

    /**
     * Get RDF property (this slot)
     */
    private RDFProperty getPredicate() {
        return getOWLModel().getRDFProperty(getSlot().getName());
    }



    private RDFSDatatype getXSDgYear() {
        return getOWLModel().getRDFSDatatypeByURI(XSDDatatype.XSDgYear.getURI());

    }

    private OWLModel getOWLModel() {
        return (OWLModel) getKnowledgeBase();
    }


    /**
     * Invalidate slot widget
     */
    private void invalidateSlot() {
        super.setInvalidValueBorder();
        getTextField().setForeground(Color.RED);
    }


    /**
     * Get text label in bold.
     */
    private String getBoldTextLabel(String inputString) {
        StringBuilder s = new StringBuilder();
        s.append("<html>")
                .append("<strong>")
                .append(INVALID_YEAR_MSG)
                .append("</strong>")
                .append("\"")
                .append(inputString)
                .append("\"")
                .append("</html>");
        return s.toString();
    }


    //Calling Protege main method for easy debugging
    public static void main(String[] args) {
        edu.stanford.smi.protege.Application.main(args);
    }
}



