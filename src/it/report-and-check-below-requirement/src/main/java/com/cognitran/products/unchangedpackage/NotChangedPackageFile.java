/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.unchangedpackage;

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
