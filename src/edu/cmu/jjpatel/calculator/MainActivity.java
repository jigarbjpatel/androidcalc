package edu.cmu.jjpatel.calculator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
/** This class implements basic functionality of calculator
 * Allows only one operator
 * Clear button clears entire screen
 * Memory buttons do not work*/
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calculator);        
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);	    
	}
	/** Gets the current value in the TextView */
	public String getBufferValue(){
		TextView inputView = (TextView) findViewById(R.id.output);    	
		return inputView.getText().toString();
	}
	/** Appends value to the TextView */
	public void appendToBuffer(String val){
		TextView inputView = (TextView) findViewById(R.id.output);
		inputView.append(val);
	}
	/** Sets the value of the TextView to new value */
	public void setBufferValue(String val){
		TextView inputView = (TextView) findViewById(R.id.output);
		inputView.setText(val);
	}

	/** Called on click of any digit and dot buttons */
	public void onDigitsButtonClick(View view){
		//determine which button was clicked
		Button b = (Button)view;
		String digit = b.getText().toString();
		//validate for max digits allowed
		if(digitsEntryValid())
			appendToBuffer(digit);
		else
			showMessage(this.getString(R.string.errmsg_max_digits));

	}
	/** Called on click of any operator button */
	public void onOperatorButtonClick(View view){
		Button b = (Button)view;
		String buffer = getBufferValue();
		//check if there is something in buffer because operator cannot be first term
		if(buffer.length() == 0){    		
			showMessage(this.getString(R.string.errmsg_wrong_format));
		}
		else{ 
			//if there is already an operator then do nothing
			if(!operatorExistsInBuffer(buffer))
				appendToBuffer(b.getText().toString());
		}
	}

	/** Called on click of Clear button */
	public void clear(View v){
		setBufferValue("");
	}
	/** Called on click of = button */
	public void calculate(View view){
		double result = 0.0;
		String buffer = getBufferValue();

		/*Pattern p = Pattern.compile("^([0-9|\\.])+([\\+|\\-|\\*|/])([0-9|\\.])+$");*/
		//Check for proper format
		Pattern p = Pattern.compile("^.+([\\+|\\-|\\*|/]).+$");
		Matcher m = p.matcher(buffer);
		if(!m.find()){
			setBufferValue("");
			showMessage(this.getString(R.string.errmsg_wrong_format));
		}
		else{
			String operator = m.group(1);
			//Get the operands	    	
			//split function requires escaping special chars + and * and hence delimiter variable used
			String delimiter = operator;
			if(operator.equals("+") || operator.equals("*"))
				delimiter = "\\" + delimiter;
			String[] operands = buffer.split(delimiter);
			double operand1 = 0;    	
			double operand2 = 0;
			try {
				operand1 = Double.parseDouble(operands[0]);
				operand2 = Double.parseDouble(operands[1]);
			} catch (Exception e) {
				Log.e("Parsing", e.getLocalizedMessage(), e);
				showMessage(this.getString(R.string.errmsg_wrong_format));
				return;
			}

			//do the required math operation
			result = computeResult(operator, operand1, operand2);
			//Finally display the result
			setBufferValue(String.valueOf(result));
		}
	}
	/** Does the required math operation on two operands only */
	protected double computeResult(String operator,
			double operand1, double operand2) {
		double result=0.0;
		switch(operator.charAt(0)){
		case '+':
			result = operand1 + operand2; 
			break;
		case '-':
			result = operand1 - operand2;
			break;
		case '*':
			result = operand1 * operand2;
			break;
		case '/':
			if(operand2 == 0)
				showMessage(this.getString(R.string.errmsg_divide_by_zero));
			else
				result = operand1 / operand2;
			break;
		default:
			break;
		}
		return result;
	}

	/** Returns true if there is already an operator in buffer*/
	private boolean operatorExistsInBuffer(String buffer) {
		if(buffer.matches("^.*[\\+|\\-|\\*|/].*$"))
			return true; 
		else
			return false;
	}

	/** Called when any digit or dot button is pressed - 
	 * Validates if the number of chars in an operand are within allowed limits*/
	private boolean digitsEntryValid() {	
		String buffer = getBufferValue();
		boolean isValid = false;
		Integer max_digits_allowed = 15; //default value which can be over-ridden using strings.xml key
		try {
			max_digits_allowed = Integer.parseInt(this.getString(R.string.max_digits_allowed));
		} catch (Exception e) {
			Log.e("Configuration", e.getLocalizedMessage(), e);
		}
		if(!operatorExistsInBuffer(buffer)){
			//check the first operand's length as there is no operator in input buffer
			if(buffer.length() < max_digits_allowed)
				isValid = true;			
		}
		else{
			//check the second operand's length
			String operator = getOperatorFromBuffer(buffer);
			if(operator != ""){				
				String operand_two;
				try {
					operand_two = buffer.substring(buffer.indexOf(operator));
					if(operand_two.length() < max_digits_allowed )
						isValid = true;	
				} catch (Exception e) {
					Log.e("Validation", e.getLocalizedMessage(),e);
				}		
			}
		}
		return isValid;
	}
	/** Returns the operator from the current string */
	private String getOperatorFromBuffer(String buffer) {		
		Pattern p = Pattern.compile("^.+([\\+|-|\\*|/]).*$");
		Matcher m = p.matcher(buffer);
		if(m.find())
			return m.group(1);
		else
			return "";
	}

	/** Helper function - shows a Toast message for short duration */
	public void showMessage(String msg){
		Toast t = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
	
	/** Called on click of M+, M- and MR buttons but they are implemented in EnhancedActivity */
	public void onMemPlusClick(View v){}
	public void onMemMinusClick(View v){}
	public void onMrcClick(View v){}

}
