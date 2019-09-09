package com.pg85.otg.customobjects.bo3.checks;

public class ModCheckNot extends ModCheck
{
    @Override
    public String makeString()
    {
        return makeString("ModCheckNot");
    }
    
    public boolean evaluate()
    {
        return !super.evaluate();
    }
}
