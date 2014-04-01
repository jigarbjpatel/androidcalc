package edu.cmu.jjpatel.calculator;

import java.util.ArrayList;
import java.util.Stack;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
/** This class adds functionality to MainActivity and overrides some
 * Allows any number of operators
 * Clear button works as both backspace and also clears entire screen on long press
 * Memory buttons are functional
 * Select and copy functionality */
public class EnhancedActivity extends MainActivity {
	double calcMemory = 0.0; //used for M+ M- and MR buttons
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calculator);
		
		selectAndCopyFunctionality();
		attachLongPressEventHandlerForClearButton();		
	}
	/** Handles Long click event of TextView and copies the selected text to clip board*/
	private void selectAndCopyFunctionality() {
		TextView tv = (TextView) findViewById(R.id.output);
		tv.setOnLongClickListener(new OnLongClickListener() {
			@Override
	        public boolean onLongClick(View v) {	        	
	            TextView tvOutput = (TextView) findViewById(R.id.output);
	            String entireText = tvOutput.getText().toString();
	            int startIndex = tvOutput.getSelectionStart();
	            int endIndex = tvOutput.getSelectionEnd();
	            try{
		            String selectedText = entireText.substring(startIndex, endIndex);
		            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		            ClipData clip = ClipData.newPlainText("Copied Text", selectedText);
		            clipboard.setPrimaryClip(clip);
	            }catch(Exception e){
	            	Log.e("Select and Copy", e.getMessage(),e);	            	
	            }
	        	return true;
	        }
		});
	}
	
	/** Attaches onLongClick event to clear button which clears the screen */
	private void attachLongPressEventHandlerForClearButton() {
		Button clearButton = (Button) findViewById(R.id.clearButton);		
		clearButton.setOnLongClickListener(new OnLongClickListener() { 
	        @Override
	        public boolean onLongClick(View v) {	        	
	            TextView tvOutput = (TextView) findViewById(R.id.output);
	            tvOutput.setText("");
	        	return true;
	        }
	    });
	}
	/** Called on click of clear button - it removes only the last character from screen*/
	@Override
	public void clear(View v){
		String buffer = super.getBufferValue();
		if(!buffer.isEmpty()){
			buffer = buffer.substring(0,buffer.length()-1);
			setBufferValue(buffer);
		}
	}
	
	/** Called on click of any digit and dot buttons */
	@Override	
	public void onDigitsButtonClick(View view){
		//determine which button was clicked
		Button b = (Button)view;
		String digit = b.getText().toString();
		appendToBuffer(digit);
	}
	/** Called on click of any operator button */
	@Override
	public void onOperatorButtonClick(View v){
		Button b = (Button)v;
		String buffer = getBufferValue();
		//check if there is something in buffer because operator cannot be first term
		if(buffer.isEmpty()){    		
			showMessage(this.getString(R.string.errmsg_wrong_format_number_first));
		}
		else{ 
			//check the last character - it should not be operator, if it is ignore the input
			if(!isOperator(buffer.substring(buffer.length()-1)))
			{
				appendToBuffer(b.getText().toString());				
			}
		}
	}
	
	/* Methods used when = is pressed*/
	/** Called on click of = button*/
	@Override 
	public void calculate(View v){		
		double result = evaluateCompleteExpression();		
		super.setBufferValue(String.valueOf(result));
	}
	/** Reads the current input and returns the result  */
	private double evaluateCompleteExpression() {
		String buffer = super.getBufferValue();
		double result = 0.0;
		if(!buffer.isEmpty()){
			//remove the first and last operator - works like trim space function but for operators
			buffer = cleanExpression(buffer);			
			//Convert normal expression to Reverse Polish Notation form
			String[] expression_in_RPN = getReversePolishNotation(buffer);
			//Evaluate RPN expression
			result = evaluateRPNExpression(expression_in_RPN);
		}
		return result;
	}
	/** Remove the trailing extra operators, if any
	 *  And if there is minus symbol as first character, adds 0 to the starting of string*/
	private String cleanExpression(String buffer) {
		String lastChar = buffer.substring(buffer.length() - 1);
		if(isOperator(lastChar)){
			buffer = buffer.substring(0, buffer.length()-1);
		}
		if(buffer.startsWith("-")){
			buffer = "0" + buffer;
		}
		return buffer;
	}
	/** Converts the expression 2+3*5 into RPN 235*+ */
	private String[] getReversePolishNotation(String buffer) {
		ArrayList<String> output = new ArrayList<String>();
		Stack<String> operatorStack = new Stack<String>();
		ArrayList<String> tokens = getTokens(buffer);
		
		for(String s : tokens){
			if(isOperator(s)){
				//check the operator stack and decide whether to push or pop
				if(!operatorStack.isEmpty()){					
					if(precedence(s) <= precedence(operatorStack.peek())){
						output.add(operatorStack.pop());
					}
				}
				operatorStack.push(s);
			}
			else{
				output.add(s);
			}
		}
		while(!operatorStack.isEmpty()){
			output.add(operatorStack.pop());
		}
		return output.toArray(new String[output.size()]);
	}
	/** Returns tokens(operators and operands) from the input expression */
	private ArrayList<String> getTokens(String buffer) {
		ArrayList<String> tokens = new ArrayList<String>();
		char[] bufferChars = buffer.toCharArray();
		String tempNumber = "";
		for(char c : bufferChars){
			if(isOperator(String.valueOf(c))){
				//add the complete number as next operator is found
				tokens.add(tempNumber);
				//add the operator also
				tokens.add(String.valueOf(c));
				tempNumber = "";
			}
			else{
				//accumulate the digits in a number
				tempNumber += c;
			}
		}
		//add the last number in tokens list
		tokens.add(tempNumber);
		return tokens;
	}
	/** Evaluates the Reverse Polish Notation expression and returns the result */
	private double evaluateRPNExpression(String[] expression_in_RPN) {
		double result=0.0;
		double temp = 0.0;
		Stack<String> numberStack = new Stack<String>();
		for(String s : expression_in_RPN){
			if(isOperator(s)){
				//when there it an operator, pop the 2 numbers from number stack, 
				//calculate and push the result back to number stack
				if(numberStack.size() < 2){
					super.showMessage(this.getString(R.string.errmsg_wrong_format_generic));
					break;
				}
				double operand1 = 0;    	
				double operand2 = 0;
				try {
					operand1 = Double.parseDouble(numberStack.pop());
					operand2 = Double.parseDouble(numberStack.pop());
				} catch (Exception e) {
					Log.e("Parsing", e.getLocalizedMessage(), e);
					showMessage(this.getString(R.string.errmsg_wrong_format_generic));
					break;
				}
				temp = super.computeResult(s,operand2,operand1);
				numberStack.push(String.valueOf(temp));
			}
			else{ // if next token is number, just push it to the number stack
				numberStack.push(s);
			}
		}
		try {
			if(!numberStack.isEmpty())
				result = Double.parseDouble(numberStack.pop());
		} catch (NumberFormatException e) {
			Log.e("Parsing", e.getLocalizedMessage(), e);
			showMessage(this.getString(R.string.errmsg_wrong_format_generic));
			result = 0.0;
		}
		return result;
	}
	/** Returns the precedence of the operator */
	private int precedence(String operator) {
		switch(operator.charAt(0)){
		case '+':
			return 1;
		case '-':
			return 2;
		case '*':
			return 3;
		case '/':
			return 4;
		default:
			return 0; 

		}		
	}
	/** Checks whether the input is an operator or not*/
	private boolean isOperator(String testString) {
		if(testString.matches("[\\+|\\-|\\*|/]"))
			return true; 
		else
			return false;
	}
	
	/* M+, M- and MRC related functions */
	/** Add the current value of expression on screen to calculator memory */
	@Override
	public void onMemPlusClick(View v){
		double result = evaluateCompleteExpression();
		calcMemory += result;		
	}	
	/** Subtract the current value of expression on screen from calculator memory */
	@Override
	public void onMemMinusClick(View v){
		double result = evaluateCompleteExpression();
		calcMemory -= result;
	}
	/** Displays the value in memory*/
	@Override
	public void onMrcClick(View v){
		super.setBufferValue(String.valueOf(calcMemory));
	}

	
}
