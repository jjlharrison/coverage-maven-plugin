package com.jjlharrison.unchangedpackage;

public class NotChangedPackageFile
{
    public String fileNotChanged(final boolean condition)
    {
        if (condition)
        {
            return "Branch 1";
        }
        else
        {
            return "Branch 2";
        }
    }
}
