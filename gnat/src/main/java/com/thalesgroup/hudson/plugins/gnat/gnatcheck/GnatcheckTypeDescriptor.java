/*******************************************************************************
* Copyright (c) 2009 Thales Corporate Services SAS                             *
* Author : Gregory Boissinot                                                   *
*                                                                              *
* Permission is hereby granted, free of charge, to any person obtaining a copy *
* of this software and associated documentation files (the "Software"), to deal*
* in the Software without restriction, including without limitation the rights *
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
* copies of the Software, and to permit persons to whom the Software is        *
* furnished to do so, subject to the following conditions:                     *
*                                                                              *
* The above copyright notice and this permission notice shall be included in   *
* all copies or substantial portions of the Software.                          *
*                                                                              *
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
* THE SOFTWARE.                                                                *
*******************************************************************************/
package com.thalesgroup.hudson.plugins.gnat.gnatcheck;

import hudson.model.Descriptor;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;

import com.thalesgroup.hudson.plugins.gnat.GnatInstallation;
import com.thalesgroup.hudson.plugins.gnat.gnatmake.GnatmakeBuilder;

public abstract class GnatcheckTypeDescriptor extends Descriptor<GnatcheckType> {
	
    protected GnatcheckTypeDescriptor(Class<? extends GnatcheckType> clazz) {
        super(clazz);
    }
    
    /**
     * Unused.
     */
    @Override
    public final GnatcheckType newInstance(StaplerRequest req) throws FormException {
        throw new UnsupportedOperationException();
    }

    /**
     * All registered {@link PortTypeDescriptor}s.
     */
    public static final List<GnatcheckTypeDescriptor> LIST = new ArrayList<GnatcheckTypeDescriptor>();    

	public GnatInstallation[] getInstallations() {
		return GnatmakeBuilder.DESCRIPTOR.getInstallations();					
	}
	
	public abstract String getHelpFile();	
}
