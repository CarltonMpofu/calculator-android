package com.example.mycalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine,
        btnAdd, btnSubtract, btnDivide, btnMultiply,btnDot, btnEquals, btnZero;

    TextView tvResult, tvDisplay;

    ImageView ivDelete;

    final int MAX_DECIMAL_PLACES = 10;
    final int MAX_NUMBER_OF_DIGITS = 15;
    final int MAX_CHARACTERS = 100;

    final boolean ADD_COMMAS = true;

    boolean showResult;
    boolean isZeroFirst;
    boolean hasLineSize;
    boolean isDividingByZero;

    BigDecimal answer;

    String currentNumberText;

    float totalCharactersPerLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnZero = findViewById(R.id.btnZero);
        btnOne = findViewById(R.id.btnOne);
        btnTwo = findViewById(R.id.btnTwo);
        btnThree = findViewById(R.id.btnThree);
        btnFour = findViewById(R.id.btnFour);
        btnFive = findViewById(R.id.btnFive);
        btnSix = findViewById(R.id.btnSix);
        btnSeven = findViewById(R.id.btnSeven);
        btnEight = findViewById(R.id.btnEight);
        btnNine = findViewById(R.id.btnNine);
        btnAdd = findViewById(R.id.btnAdd);
        btnSubtract = findViewById(R.id.btnSubtract);
        btnMultiply = findViewById(R.id.btnMultiply);
        btnDivide = findViewById(R.id.btnDivide);
        btnDot = findViewById(R.id.btnDot);
        btnEquals = findViewById(R.id.btnEquals);

        tvResult = findViewById(R.id.tvResult);
        tvDisplay = findViewById(R.id.tvDisplay);

        ivDelete = findViewById(R.id.ivDelete);

        tvResult.setText("");
        tvDisplay.setText("");

        showResult = false;
        isZeroFirst = false;
        hasLineSize = false;
        isDividingByZero = false;

        answer = new BigDecimal(0);

        currentNumberText = "";

        tvDisplay.measure(0,0);

        tvDisplay.setMovementMethod(new ScrollingMovementMethod());

        tvDisplay.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float totalTextSize = tvDisplay.getPaint().measureText(tvDisplay.getText().toString());
                float textViewWidth = tvDisplay.getMeasuredWidth();

                if(totalTextSize <= textViewWidth)
                {
                    if(hasLineSize && count <= totalCharactersPerLine)
                    { // Make text size bigger
                        tvDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 45f);
                        hasLineSize = false;
                    }

                }
                else if(totalTextSize > textViewWidth)
                {
                    if(hasLineSize == false)
                    { // Make text size smaller
                        hasLineSize = true;
                        totalCharactersPerLine = tvDisplay.getLayout().getLineEnd(0);
                        tvDisplay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35f);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Digits

        btnZero.setOnClickListener(v ->
        { // Add zero to arithmetic expression

            String arithmeticExpression = tvDisplay.getText().toString().trim();

            // Remove commas from numbers in arithmetic expression
            arithmeticExpression = addOrRemoveCommas(arithmeticExpression, !ADD_COMMAS);

            // Change the text color of tvDisplay back to white
            tvDisplay.setTextColor(getResources().getColor(R.color.white));

            if(getCurrentNumberLength() >= MAX_NUMBER_OF_DIGITS)
            {
                Toast.makeText(MainActivity.this,
                        "Maximum number of digits is " + MAX_NUMBER_OF_DIGITS, Toast.LENGTH_SHORT).show();
            }
            else if(getNumberOfDecimalPlaces() >= MAX_DECIMAL_PLACES)
            {
                Toast.makeText(MainActivity.this,
                        "Maximum number of digits after decimal point is " + MAX_DECIMAL_PLACES, Toast.LENGTH_SHORT).show();
            }
            else  if(getTotalNumberOfCharacters() >= MAX_CHARACTERS)
            {
                Toast.makeText(MainActivity.this,
                        "Maximum number of characters is " + MAX_CHARACTERS, Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(currentNumberText.length() == 1 && currentNumberText.charAt(0) == '0')
                {
                    // Already added zero
                    // Do nothing
                }
                else
                { // Add zero to arithmetic expression
                    arithmeticExpression += "0";
                    currentNumberText += "0";
                }

                // Add commas to numbers in arithmetic expression
                arithmeticExpression = addOrRemoveCommas(arithmeticExpression, ADD_COMMAS);

                // Change the color of the operators
                String text = addColorToOperators(arithmeticExpression);

                tvDisplay.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
                tvDisplay.bringPointIntoView(tvDisplay.length());


                if (hasOperator())
                { // arithmetic expression has operators, Can calculate answer
                    calculateAnswer();

                    if(isDividingByZero) // Cannot divide by zero in arithmetic expression
                        tvResult.setText("");
                    else // Add commas to the answer and display it
                        tvResult.setText(addCommasToNumber(answer.toString()));
                }
                else
                {
                    tvResult.setText("");
                }
            }
        });


        btnOne.setOnClickListener(v -> addDigitToExpression("1"));

        btnTwo.setOnClickListener(v -> addDigitToExpression("2"));

        btnThree.setOnClickListener(v -> addDigitToExpression("3"));

        btnFour.setOnClickListener(v -> addDigitToExpression("4"));

        btnFive.setOnClickListener(v -> addDigitToExpression("5"));

        btnSix.setOnClickListener(v -> addDigitToExpression("6"));

        btnSeven.setOnClickListener(v -> addDigitToExpression("7"));

        btnEight.setOnClickListener(v -> addDigitToExpression("8"));

        btnNine.setOnClickListener(v -> addDigitToExpression("9"));

        // Dot

        btnDot.setOnClickListener(v ->
        {
            // Change color of textview(tvDisplay) back to white
            tvDisplay.setTextColor(getResources().getColor(R.color.white));

            // Get arithmetic expression from the textview (tvDisplay)
            String arithmeticExpression = tvDisplay.getText().toString().trim();

            // Remove commas
            arithmeticExpression = addOrRemoveCommas(arithmeticExpression, !ADD_COMMAS);


            if(!hasDecimalPoint())
            {
                if(currentNumberText.isEmpty())
                {
                    arithmeticExpression += "0.";
                    currentNumberText += "0.";
                }
                else
                {
                    arithmeticExpression += ".";
                    currentNumberText += ".";
                }
            }

            // Add commas
            arithmeticExpression = addOrRemoveCommas(arithmeticExpression, ADD_COMMAS);

            // Change the color of the operators in the arithmetic expression and display the
            // arithmetic expression on the textview (tvDisplay)
            tvDisplay.setText(Html.fromHtml(addColorToOperators(arithmeticExpression)), TextView.BufferType.SPANNABLE);

        });

        // Operators

        btnAdd.setOnClickListener(v -> addOperatorToExpression("+"));

        btnSubtract.setOnClickListener(v -> addOperatorToExpression("-"));

        btnMultiply.setOnClickListener(v -> addOperatorToExpression("×"));

        btnDivide.setOnClickListener(v -> addOperatorToExpression("÷"));

        // Remove single digit or operator

        ivDelete.setOnClickListener(v ->
        {
            // Get the arithmetic expression from the textview (tvDisplay)
            String arithmeticExpression = tvDisplay.getText().toString().trim();

            // Remove commas
            arithmeticExpression = addOrRemoveCommas(arithmeticExpression, !ADD_COMMAS);

            if(!arithmeticExpression.isEmpty())
            {
                // remove last value in expression
                arithmeticExpression = arithmeticExpression.substring(0, arithmeticExpression.length()-1);
                String newText = addOrRemoveCommas(arithmeticExpression, ADD_COMMAS);

                // Change color of the operators and update textview (tvDisplay)
                tvDisplay.setText(Html.fromHtml(addColorToOperators(newText)), TextView.BufferType.SPANNABLE);

                if(hasOperator() && !arithmeticExpression.isEmpty())
                {

                    // use the position of the last operator in the arithmetic expression
                    // to find the last number entered
                    int idxAdd = arithmeticExpression.lastIndexOf('+');
                    int idxSubtract = arithmeticExpression.lastIndexOf('-');
                    int idxMultiply = arithmeticExpression.lastIndexOf('×');
                    int idxDivide = arithmeticExpression.lastIndexOf('÷');

                    int idx = Math.max(idxDivide ,Math.max(idxMultiply,Math.max(idxAdd, idxSubtract)));

                    // get the last number
                    currentNumberText = arithmeticExpression.substring(idx+1);

                    char last = arithmeticExpression.charAt(arithmeticExpression.length()-1);
                    if(last == '+' || last == '-' || last == '×' || last == '÷')
                    {
                        tvResult.setText("");
                        currentNumberText = "";
                    }
                    else
                    {
                        calculateAnswer();
                        if(isDividingByZero) // Cannot divide by zero
                            tvResult.setText("");
                        else
                        {
                            String myAnswer;
                            if(answer.compareTo(new BigDecimal("999999999999999")) > 0)
                            // answer too big, change to scientific notation
                            {
                                myAnswer = format(answer);
                            }
                            else if(answer.toString().length() > 15)
                            { // Answer too long, change to scientific notation
                                int scale = 14 - (answer.precision() - answer.scale());
                                scale = Math.max(0, scale);
                                myAnswer = answer.setScale(scale, RoundingMode.FLOOR).toString();
                            }
                            else
                                myAnswer = answer.toString();

                            //  Add commas to numbers and display the answer
                            tvResult.setText(addOrRemoveCommas(myAnswer, ADD_COMMAS));
                        }

                    }

                }
                else
                { // No answer to calculate
                    currentNumberText = arithmeticExpression;
                    tvResult.setText("");
                }
            }
        });

        // Equals
        btnEquals.setOnClickListener(v ->
        {
            if(isDividingByZero)
            {
                Toast.makeText(MainActivity.this, "Cannot divide by 0", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(tvResult.getText().equals(""))
                {
                    // Nothing
                }
                else
                {
                    tvDisplay.setText(tvResult.getText());
                    currentNumberText = tvResult.getText().toString();

                    // Change the textview(tvDisplay) color to dark_primary_color
                    tvDisplay.setTextColor(getResources().getColor(R.color.dark_primary_color));
                    tvResult.setText("");
                }
            }

        });
    }

    /**
     * Changes the color of the operators (+, -, ×, ÷) in the arithmetic expression
     * @param expression
     * String: The arithmetic expression
     * @return
     * String: a new arithmetic expression where the color of the operators is changed
     */
    private String addColorToOperators(String expression)
    {
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < expression.length();i++)
        {
            char c = expression.charAt(i);

            if(c == '-' || c == '+' || c == '×' || c == '÷')
            { // Change the color of the operator
                String text = "<font color='#38414F'>"+ c +"</font>";
                result.insert(result.length(), text);
            }
            else
            {
                result.insert(result.length(), c);
            }

        }

        return result.toString();
    } // addColorToOperators

    /**
     * Removes commas from the number if it has more than three digits.
     * @param number
     * String : The number to remove commas from
     * @return
     * String : the new number with commas removed from it, if it has more than three digits
     */
    private String removerCommasFromNumber(String number)
    {
        StringBuilder result = new StringBuilder();

        // Separate digits before and after decimal point
        String[] numberArray =  number.split("\\.");

        // Only remove commas from digits before decimal point
        String number0 = numberArray[0];
        for(int i = 0; i < number0.length(); i++)
        {
           char c = number0.charAt(i);
           if(c != ',')
           {
               result.insert(result.length(), c);
           }
        }

        if(number.contains("."))
        { // if the number has decimal point then add the decimal point to the result
            result.insert(result.length(), '.');
        }
        if(numberArray.length > 1)
        // number had decimal point so combine the digits after the decimal point with the digits
        // before the decimal point, separated by a '.'
        {
            result.insert(result.length(), numberArray[1]);
        }

        return result.toString();
    } // removerCommasFromNumber

    /**
     * Adds commas to the number if it has more than three digits.
     * @param number
     * String : The number to add commas to
     * @return
     * String : the new number with commas added to it if it has more than three digits
     */
    private String addCommasToNumber(String number)
    {
        StringBuilder result = new StringBuilder();

        int counter = 0;

        // Separate digits before and after decimal point
        String[] numberArray =  number.split("\\.");

        // Only add commas to digits before decimal point
        String number0 = numberArray[0];
        for(int i = number0.length()-1; i >= 0; i--)
        {
            if(counter >= 3)
            { // Add the comma
                counter = 0;
                result.insert(0, ",");
            }
            result.insert(0, number0.charAt(i));
            counter++;
        }

        if(number.contains("."))
        { // if the number has decimal point then add the decimal point to the result
            result.insert(result.length(), '.');
        }
        if(numberArray.length > 1)
        // number had decimal point so combine the digits after the decimal point with the digits
        // before the decimal point, separated by a '.'
        {
            result.insert(result.length(), numberArray[1]);
        }
        return result.toString();
    } // addCommasToNumber

    /**
     * Adds or removes commas to or from numbers in the arithmetic expression depending on the boolean
     * variable (addCommas). Only adds commas to numbers with more than three digits. For example:
     * 1000 -> 1,000 : 2121234 -> 2,121,234 : 100 -> 100. Also removes commas from numbers with more
     * than three digits.
     * @param arithmeticExpression
     * String : The arithmetic expression to evaluate
     * @param addCommas
     * boolean : The boolean variable that determines whether to add(true) commas or remove(false) commas
     * @return
     * String: a new arithmetic expression, where commas are added to numbers, if addCommas is true.
     * Or where commas are removed from numbers, if addCommas is false;
     */
    private String addOrRemoveCommas(String arithmeticExpression, boolean addCommas)
    {
        StringBuilder result = new StringBuilder();
        String numText = "";
        for(int i =0; i < arithmeticExpression.length(); i++)
        {
            char c = arithmeticExpression.charAt(i);
            if(c == '+' || c == '-' || c == '×' || c == '÷')
            {
                if(addCommas) // Add commas to the number
                    result.append(addCommasToNumber(numText)).append(c);
                else // Remove commas from the number
                    result.append(removerCommasFromNumber(numText)).append(c);
                numText = "";
            }
            else
            {
                numText += c;
            }
        }

        if(addCommas) // Add commas to the number
            result.append(addCommasToNumber(numText));
        else // Remove commas from the number
            result.append(removerCommasFromNumber(numText));

        return result.toString();
    } // addOrRemoveCommas


    /**
     * Adds the specified operator to the arithmetic expression
     * @param operator
     * String : The operator to add
     */
    private void addOperatorToExpression(String operator) {
        // Change the color of the textview (tvDisplay) back to white
        tvDisplay.setTextColor(getResources().getColor(R.color.white));

        // Get the arithmetic expression from the textview (tvDisplay)
        String arithmeticExpression = tvDisplay.getText().toString().trim();

        if(!arithmeticExpression.isEmpty())
        {
            char last = arithmeticExpression.charAt(arithmeticExpression.length()-1);
            if(last == '-' || last == '+' || last == '×' || last == '÷')
            // if the last thing (in arithmetic expression) is an operator then remove it
            // to avoid having two operators next to each other
            {
                arithmeticExpression = arithmeticExpression.substring(0, arithmeticExpression.length()-1);
            }

            // Add operator to arithmetic expression
            arithmeticExpression += operator;

            // Change the color of the operators in the arithmetic expression and then update
            // the textview (tvDisplay) to display it
            tvDisplay.setText(Html.fromHtml(addColorToOperators(arithmeticExpression)), TextView.BufferType.SPANNABLE);

            // Reset for the next number
            currentNumberText = "";

            tvResult.setText("");

        }
    } // addOperatorToExpression

    /**
     * Changes the specified bigDecimal into scientific notation
     * @param bigDecimal
     * @return
     * String : The bigDecimal in scientific notation
     */
    private String format(BigDecimal bigDecimal)
    {
        NumberFormat numberFormat = new DecimalFormat("0.0E0");
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        numberFormat.setMinimumFractionDigits(8);
        String result = numberFormat.format(bigDecimal);
        if(result.contains(","))
        { // if used comma instead of dot then change the comma into a dot
            result = result.replace(',', '.');
        }
        return  result;

    } // format

    /**
     * Adds the specified digit (e.g. 1, 2, 3, 4, 5, 6, 7, 8 and 9) to the arithmetic expression.
     * And calculates the answer or evaluates the arithmetic expression to find the answer
     * @param digit
     * String : The digit to add to the arithmetic expression
     */
    private void addDigitToExpression(String digit) {

        // Set color of textview (tvDisplay) back to white
        tvDisplay.setTextColor(getResources().getColor(R.color.white));

        // Get the arithmetic expression from the textview (tvDisplay)
        String arithmeticExpression = tvDisplay.getText().toString().trim();

        //  Remove commas from numbers in arithmetic expression
        arithmeticExpression = addOrRemoveCommas(arithmeticExpression, !ADD_COMMAS);

        if(getCurrentNumberLength() >= MAX_NUMBER_OF_DIGITS)
        {
            Toast.makeText(MainActivity.this,
                    "Maximum number of digits is " + MAX_NUMBER_OF_DIGITS, Toast.LENGTH_SHORT).show();
        }
        else if(getNumberOfDecimalPlaces() >= MAX_DECIMAL_PLACES)
        {
            Toast.makeText(MainActivity.this,
                    "Maximum number of digits after decimal point is " + MAX_DECIMAL_PLACES, Toast.LENGTH_SHORT).show();
        }
        else  if(getTotalNumberOfCharacters() >= MAX_CHARACTERS)
        {
            Toast.makeText(MainActivity.this,
                    "Maximum number of characters is " + MAX_CHARACTERS, Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(currentNumberText.length() == 1 && currentNumberText.charAt(0) == '0')
            { // If the number starts with zero then remove the zero
                arithmeticExpression = arithmeticExpression.substring(0, arithmeticExpression.length()-1);
                currentNumberText = "";
            }

            // add the digit
            arithmeticExpression += digit;
            currentNumberText += digit;

            // Add commas to the numbers in the arithmetic expression
            arithmeticExpression = addOrRemoveCommas(arithmeticExpression, ADD_COMMAS);

            // Update the arithmetic expression on the textview (tvDisplay)
            tvDisplay.setText(Html.fromHtml(addColorToOperators(arithmeticExpression)), TextView.BufferType.SPANNABLE);
            tvDisplay.bringPointIntoView(tvDisplay.length());

            if (hasOperator())
            { // Can calculate answer

                calculateAnswer();
                if(isDividingByZero) // STOP! Cannot divide by zero
                    tvResult.setText("");
                else
                {
                    String myAnswer;

                    if(answer.compareTo(new BigDecimal("999999999999999")) > 0)
                    { // answer too big. Change it to scientific notation
                        String result = format(answer);
                        myAnswer = result;
                    }
                    else if(answer.toString().length() > 15)
                    { // answer too long. Change it to scientific notation
                        int scale = 14 - (answer.precision() - answer.scale());
                        scale = Math.max(0, scale);
                        myAnswer = answer.setScale(scale, RoundingMode.FLOOR).toString();
                    }
                    else
                    {
                        myAnswer = answer.toString();
                    }
                    // Add commas to the answer and display it
                    tvResult.setText(addOrRemoveCommas(myAnswer, ADD_COMMAS));
                }

            }
            else
            { // no operators. no answer to calculate
                tvResult.setText("");
            }
        }
    } // addDigitToExpression

    /**
     * Get the total number of characters in the arithmeticl expression
     * @return
     * int : the total number of characters
     */
    int getTotalNumberOfCharacters()
    {
        return tvDisplay.getText().length();
    } // getTotalNumberOfCharacters

    /**
     * Get the length of the current number
     * @return
     * int : The length of the current number
     */
    int getCurrentNumberLength()
    {
        return currentNumberText.length();
    } // getCurrentNumberLength

    /**
     * Get the number of digits after the decimal point
     * @return
     * int : the number of digits after the decimal place
     */
    int getNumberOfDecimalPlaces()
    {
        if(currentNumberText.equals(""))
        {
            return 0;
        }
        return new BigDecimal(currentNumberText).scale();
    } // getNumberOfDecimalPlaces

    /**
     * checks if the current number has a decimal point
     * @return
     * boolean : true if the current number has a decimal point. false it has no decimal point
     */
    boolean hasDecimalPoint()
    {
        return currentNumberText.contains(".");
    } // hasDecimalPoint

    /**
     * Evaluates the arithmetic expression to find the answer.
     * Stores the answer in a global variable called answer
     */
    void calculateAnswer()
    {
        // Change text color of textview (tvDisplay) back to white
        tvDisplay.setTextColor(getResources().getColor(R.color.white));

        // Get arithmetic expression from textview (tvDisplay)
        String arithmeticExpression = tvDisplay.getText().toString().trim();

        // Remove commas from number in the arithmetic expression
        arithmeticExpression = addOrRemoveCommas(arithmeticExpression, !ADD_COMMAS);

        ArrayList<String> myValues = new ArrayList<>();

        if(!arithmeticExpression.isEmpty())
        { // Add numbers and operates to myValues array list
            StringBuilder numText = new StringBuilder();

            for(int idx = 0; idx < arithmeticExpression.length(); idx++)
            {
                char c = arithmeticExpression.charAt(idx);
                if(c == '+' || c == '-' || c == '×' || c == '÷')
                {
                    myValues.add(numText.toString());
                    myValues.add(Character.toString(c));
                    numText = new StringBuilder();
                }
                else
                {
                    numText.append(c);
                }
            }
            myValues.add(numText.toString());
        }

        // BODMAS
        isDividingByZero = false;
        // Division first
        bodmas(myValues, "÷");

        if(isDividingByZero) // STOP! Invalid expression that divides by zero
            return;

        // Multiply second
        bodmas(myValues, "×");

        // Addition and Subtraction have same rank
        additionAndSubtraction(myValues);

        answer = new BigDecimal(myValues.get(0));

        answer = answer.stripTrailingZeros();
        // stripTrailingZeros forces numbers like 10, 10.00, 20,00.000, 1000 into scientific notation

        if(answer.toString().contains("E"))
        {  // Change number back to fixed-point number
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            answer = new BigDecimal(decimalFormat.format(answer));
        }
    } // calculateAnswer

    /**
     * Apply bodmas rules when evaluating the arithmetic expression.
     * Depending on whether the operator is division(÷) or multiplication(×)
     * @param arithmeticExpression
     * String : The arithmetic expression to evaluate
     * @param operator
     * String : The operator that determines which operation to do
     */
    private void bodmas(ArrayList<String> arithmeticExpression, String operator)
    {
        int index;

        // find the position of the first occurrence of the  operator in the arithmetic expression
        index = arithmeticExpression.indexOf(operator);

        while (index != -1)
        {
            // Get the numbers to operate on
            BigDecimal num1 = new BigDecimal(arithmeticExpression.get(index-1));
            BigDecimal num2 = new BigDecimal(arithmeticExpression.get(index+1));

            if(operator.equals("÷"))
            { // Divide
                if(num2.toString().equals("0"))
                { // Cannot divide by zero
                    isDividingByZero = true;
                    return;
                }
                if(isDividingByZero == false)
                { // Divide num1 by num2
                    num1 = num1.divide(num2, 10, RoundingMode.HALF_UP);
                }

            }
            else // Multiply
            { // Multiply num1 by num2
                num1 = num1.multiply(num2);
            }

            // remove the operator and the operands
            arithmeticExpression.remove(index-1);
            arithmeticExpression.remove(index - 1);
            arithmeticExpression.remove(index - 1);

            // Add the answer to the arithmetic expression
            arithmeticExpression.add(index-1, num1.toString());

            // find the next position of the operator
            index = arithmeticExpression.indexOf(operator);
        }
    } // bodmas

    /**
     * Adds or subtracts the values in the arithmetic expression depending on which operator
     * occurs first
     * @param arithmeticExpression
     * ArrayList<String> : The arithmetic expression to evaluate
     */
    void additionAndSubtraction(ArrayList<String> arithmeticExpression)
    {
        while(arithmeticExpression.size() > 1)
        {
            int index = 1;

            BigDecimal num1 = new BigDecimal(arithmeticExpression.get(index-1));
            BigDecimal num2 = new BigDecimal(arithmeticExpression.get(index+1));
            if(arithmeticExpression.get(index).equals("+"))
            { // add values
                num1 = num1.add(num2);
            }
            else
            { // subtract values
                num1 = num1.subtract(num2);
            }

            // remove both numbers and the operator
            arithmeticExpression.remove(index-1);
            arithmeticExpression.remove(index - 1);
            arithmeticExpression.remove(index - 1);

            // add the result of the operation to the arithmetic expression
            arithmeticExpression.add(index-1, num1.toString());
        }
    } // additionAndSubtraction

    /**
     * Checks if the arithmetic expression has any operators (+, -, ×, ÷)
     * @return True: if the arithmetic expression has an operators. False: if there are none
     */
    private boolean hasOperator()
    {
        String text = tvDisplay.getText().toString().trim();
        if(text.isEmpty())
            return false;
        for(int idx = 0; idx  < text.length(); idx++)
        {
            char c = text.charAt(idx);
            if(c == '+' || c == '-' || c == '×'  || c == '÷')
            {
                return true;
            }
        }
        return false;
    } // hasOperator
}