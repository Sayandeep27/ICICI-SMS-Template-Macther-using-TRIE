package com.sms.matcher;

import com.sms.matcher.model.SmsTemplate;
import com.sms.matcher.repository.SmsTemplateRepository;
import com.sms.matcher.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final SmsTemplateRepository templateRepository;
    private final TemplateService templateService;

    @Override
    public void run(String... args) {
        List<SmsTemplate> seedTemplates = List.of(
            new SmsTemplate(1L,
                "Dear Customer,your OTP is {#var},valid for {#var} mins."),
            new SmsTemplate(2L,
                "Amount Rs {#var} debite to your account"),
            new SmsTemplate(3L,
                "Amount Rs {#var} debited to your account.Your available balance is Rs {#var}"),
            new SmsTemplate(4L,
                "GST returns for your ICICI Bank Business Installment Loan Application number {#var} have been successfully fetched."),
            new SmsTemplate(5L,
                "Dear Customer, Important action required: Your ICICI Bank Credit Card is approved. Please enable 'Online' transactions after 60 minutes on card to use it for online shopping. Follow these steps at {#var}. T and C apply"),
            new SmsTemplate(6L,
                "You have successfully deleted {#var} from your ICICI Bank Corporate Internet Banking payee list on {#var}. If not done by you, call {#var}."),
            new SmsTemplate(7L,
                "Dear Customer, Access your Loan details through iMobile app. To download, click {#var}."),
            new SmsTemplate(8L,
                "Dear {#var}, {#var} has applied for an ICICI Bank Plus Card for you. To complete the process and get your card, please click on the consent link sent to your email."),
            new SmsTemplate(9L,
                "EXCEPTION:The input is not a valid Base-64 string as it contains a non-base 64 character, more than two padding characters, or an illegal character among the padding characters."),
            new SmsTemplate(10L,
                "EXCEPTION:The input is not a valid Base-64 string as it contains a non-base 64 character, more than two padding characters, or an illegal character among the padding characters."),
            new SmsTemplate(11L,
                "To resume the application for your ICICI Bank Credit Card, please visit the website at {#var}."),
            new SmsTemplate(12L,
                "EXCEPTION:The input is not a valid Base-64 string as it contains a non-base 64 character, more than two padding characters, or an illegal character among the padding characters."),
            new SmsTemplate(13L,
                "EXCEPTION:The input is not a valid Base-64 string as it contains a non-base 64 character, more than two padding characters, or an illegal character among the padding characters."),
            new SmsTemplate(14L,
                "ICICIBANK IAUTO ALERT {#var} Ended Not OK {#var}."),
            new SmsTemplate(15L,
                "Dear Customer, Important action required: Your Amazon Pay ICICI Bank Credit Card is approved. Please enable Online transactions on card to use it for online shopping. Follow these steps at {#var}. T&C apply."),
            new SmsTemplate(16L,
                "Payment of INR {#var} towards Merchant {#var} for Standing Instruction {#var} on ICICI Bank Credit Card {#var} could not be processed. To manage your Standing Instructions, visit {#var}. Call {#var} for queries."),
            new SmsTemplate(17L,
                "To process loan application {#var} under Home Loan Mortgages, virtual property visit is initiated. Click {#var}. - ICICI Bank"),
            new SmsTemplate(18L,
                "Card usage settings of your ICICI Bank Debit Card {#var} have been successfully updated. For more details, call Customer Care."),
            new SmsTemplate(19L,
                "Payment of INR {#var} towards Merchant {#var} to be debited from ICICI Bank Debit Card {#var}, as per Standing Instruction {#var}, is due by {#var}. To cancel this debit or your Standing Instructions, visit {#var}. Call {#var} for queries."),
            new SmsTemplate(20L,
                "Dear Customer, as per your request, the card usage settings have been successfully updated on your ICICI Bank Debit Card {#var}. For details, call Customer Care."),
            new SmsTemplate(21L,
                "Out for Delivery: Cheque Book for ICICI Bank Account {#var} is out for delivery today through {#var} Courier, AWB {#var}."),
            new SmsTemplate(22L,
                "Dear {#var}, File name {#var} (Customer Ref No: {#var}) is Authorized. Success Count {#var}, Success Amount Rs. {#var}. Sincerely, CMS Disbursements, Team ICICI Bank."),
            new SmsTemplate(23L,
                "{#var} has requested you to pay Rs. {#var}. Visit {#var} to pay using ICICI Bank Merchant Solutions."),
            new SmsTemplate(24L,
                "Dear Customer, Congratulations! Business Credit Card has been set-up against application {#var}. For details, Please login to Internet Banking."),
            new SmsTemplate(25L,
                "We have initiated your application for ICICI Bank Business Card. Visit {#var} to complete the process."),
            new SmsTemplate(26L,
                "Dear Customer, Thank you for your interest in ICICI Bank's Commercial Business Loan. Your application is almost complete. Please visit {#var} to verify your details."),
            new SmsTemplate(27L,
                "By entering OTP, you consent for loan booking, EMI debit from ICICI Bank <{#var}> Acc, Key Fact Statement & accept Cardless EMI T&Cs. Visit {#var}."),
            new SmsTemplate(28L,
                "Dear Customer, address PIN Code for ICICI Bank Credit Card {#var} has been updated on {#var}. If not requested by you, please call our Customer Care."),
            new SmsTemplate(29L,
                "ICICI Bank - Personal, Business, Corporate and NRI Banking Online"),
            new SmsTemplate(30L,
                "ICICI Bank, India's trusted bank, offers personal & business banking services like savings accounts, loans, credit cards, insurance, and investment products.")
        );

        Set<Long> existingIds = templateRepository.findAll()
                .stream()
                .map(SmsTemplate::getId)
                .collect(Collectors.toSet());

        List<SmsTemplate> missingTemplates = seedTemplates.stream()
                .filter(template -> !existingIds.contains(template.getId()))
                .toList();

        if (missingTemplates.isEmpty()) {
            log.info("All seed templates already exist in DB. Skipping seed.");
            return;
        }

        templateRepository.saveAll(missingTemplates);
        templateService.rebuildTrie();

        log.info("Seeded {} missing templates.", missingTemplates.size());
    }
}
