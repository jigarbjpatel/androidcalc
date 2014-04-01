There are 2 activities in project - MainActivity and EnhancedActivity

MainActivity contains the basic functionality without any Bells and Whistles
EnhancedActivity contains following Bells and Whistles
1. Allows any number of operators
2. Memory function implementation (M+, M-, MR)
3. Clear button acts as both backspace and full clear depending on short or long click
4. Select and Copy functionality

EnhancedActivity extends MainActivity - so both are required to run the applicaiton. By default, Enhanced activity will run.

Assumptions
1. Pressing Double Zero button will append "00" to the output Textview
2. MRC button is implemented as memory recall. It does not clear the calculator memory. Memory value is not cleared even on Clear button click.

 
There is some trace file error coming on my mahcine. But it does not hamper the running of application.


References (other than developer.android.com)
1. For Shunting Yard Algorithm (to convert infix expression to postfix notation)
	http://en.wikipedia.org/wiki/Shunting-yard_algorithm
2. To detect long press event
	http://stackoverflow.com/questions/4402740/android-long-click-on-a-button-perform-actions
3. To implement select and copy functionality
	http://stackoverflow.com/questions/6624763/android-copy-to-clipboard-selected-text-from-a-textview?lq=1