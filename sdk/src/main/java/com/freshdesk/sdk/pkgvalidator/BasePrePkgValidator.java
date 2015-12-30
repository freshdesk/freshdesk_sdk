package com.freshdesk.sdk.pkgvalidator;

import com.freshdesk.sdk.ManifestContents;
import com.freshdesk.sdk.IParamContents;
import java.io.File;

/**
 *
 * @author subhash
 */
public abstract class BasePrePkgValidator implements PrePkgValidator {
    
    protected File prjDir;
    protected ManifestContents manifest;
    protected IParamContents iparams;

    @Override
    public void setPrjDir(File prjDir) {
        this.prjDir = prjDir;
    }

    @Override
    public void setManifest(ManifestContents mf) {
        this.manifest = mf;
    }

    @Override
    public void setIParam(IParamContents ip) {
        this.iparams = ip;
    }
    
}
