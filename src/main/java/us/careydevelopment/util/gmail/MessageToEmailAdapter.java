package us.careydevelopment.util.gmail;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.careydevelopment.util.gmail.model.Email;

import java.util.Base64;
import java.util.List;

public class MessageToEmailAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageToEmailAdapter.class);

    private static final String SUBJECT_HEADER = "Subject";
    private static final String FROM_HEADER = "From";
    private static final String TO_HEADER = "To";
    private static final String DATE_HEADER = "Date";

    private Message message;
    private Email email;
    private boolean lightweight = false;

    public MessageToEmailAdapter(Message message, boolean lightweight) {
        this.message = message;
        this.lightweight = lightweight;
    }

    public Email adapt() {
        email = new Email();

        if (!lightweight) {
            setEmailBody();
        }

        email.setId(message.getId());
        if (message.getSnippet() != null) {
            String snippet = message.getSnippet();
            email.setSnippet(removeZeroWidthNonJoiners(snippet));
        }

        setValuesFromHeaders();

        return email;
    }

    private void setEmailBody() {
        email.setHtml(getBody("text/html"));
        email.setPlainText(getBody("text/plain"));

        if (StringUtils.isEmpty(email.getHtml()) && StringUtils.isEmpty(email.getPlainText())) {
            email.setPlainText(getData());
        }
    }

    private String getBody(String type) {
        StringBuilder sb = new StringBuilder();

        if (message.getPayload() != null && message.getPayload().getParts() != null) {
            for (MessagePart msgPart : message.getPayload().getParts()) {
                if (msgPart.getMimeType().contains(type))
                    sb.append((new String(Base64.getUrlDecoder().decode(msgPart.getBody().getData()))));
            }
        }

        String body = sb.toString();

        return body;
    }

    private String getData() {
        StringBuilder sb = new StringBuilder();

        if (message.getPayload() != null && message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
            sb.append((new String(Base64.getUrlDecoder().decode(message.getPayload().getBody().getData()))));
        }

        String body = sb.toString();

        return body;
    }

    private void setValuesFromHeaders() {
        if (message.getPayload() != null) {
            List<MessagePartHeader> headers = message.getPayload().getHeaders();

            headers.forEach(header -> {
                setValueFromHeader(header);
            });
        }
    }

    private void setValueFromHeader(MessagePartHeader header) {
        if (header.getName() != null) {
            switch (header.getName()) {
                case  SUBJECT_HEADER:
                    email.setSubject(header.getValue());
                    break;
                case  FROM_HEADER:
                    email.setFrom(header.getValue());
                    break;
                case  TO_HEADER:
                    email.setTo(header.getValue());
                    break;
                case  DATE_HEADER:
                    email.setDate(DateUtil.getLongValueFromGmailDateFormat(header.getValue()));
                    break;
            }
        }
    }

    private String removeZeroWidthNonJoiners(String str) {
        String updated = null;

        if (str != null) {
            updated = str.replaceAll("[\\p{Cf}]", "").trim();
        }

        return updated;
    }
}
