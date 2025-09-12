package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;

public class BaseController {
    protected  final AppContext ctx;
    protected final Navigation nav;

    protected  BaseController(AppContext ctx, Navigation nav){
        this.ctx = ctx;
        this.nav = nav;
    }

}
