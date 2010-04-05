/*
 * Copyright (c) 2010. Andrew McCall [andrew@andrewmccall.com] - All Rights Reserved.
 *
 * Unless explicitly stated otherwise, all rights are owned by or controlled by Andrew McCall.
 *
 * Except as otherwise expressly permitted under copyright law the content not be copied, reproduced,
 * republished, downloaded, posted, broadcast or transmitted in any way without first obtaining Andrew
 * McCall's written permission or that of the copyright owner.
 */

package com.andrewmccall.accounts.core;

/**
 * Thrown when there is a problem with an entry.
 */
public class AccountsException extends Exception {
    
    private static final long serialVersionUID = 6651269466234956540L;

    public AccountsException(String message) {
        super(message);
    }

    public AccountsException(String message, Throwable t) {
        super(message, t);
    }
}
