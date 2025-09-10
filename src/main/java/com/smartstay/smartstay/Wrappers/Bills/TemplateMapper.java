package com.smartstay.smartstay.Wrappers.Bills;

import com.smartstay.smartstay.dao.BillTemplates;
import com.smartstay.smartstay.responses.templates.TemplateTypes;
import com.smartstay.smartstay.responses.templates.Templates;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class TemplateMapper implements Function<BillTemplates, Templates> {


    @Override
    public Templates apply(BillTemplates billTemplates) {

        List<TemplateTypes> listTemplates = billTemplates
                .getTemplateTypes()
                .stream()
                .map(item -> new TemplateTypeMapper().apply(item))
                .toList();

        return new Templates(billTemplates.getTemplateId(),
                billTemplates.getHostelId(),
                billTemplates.getDigitalSignature(),
                billTemplates.getHostelLogo(),
                billTemplates.getEmailId(),
                billTemplates.getMobile(),
                Utils.dateToString(billTemplates.getCreatedAt()),
                billTemplates.isLogoCustomized(),
                billTemplates.isMobileCustomized(),
                billTemplates.isEmailCustomized(),
                billTemplates.isSignatureCustomized(),
                listTemplates);
    }
}
