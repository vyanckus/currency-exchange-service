package ru.skillbox.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.skillbox.currency.exchange.entity.Currency;
import ru.skillbox.currency.exchange.repository.CurrencyRepository;
import ru.skillbox.currency.exchange.xml.ValuteCurs;
import ru.skillbox.currency.exchange.xml.ValuteCursList;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyLoaderService {

    private final CurrencyRepository repository;

    @Value("${cbr.url}")
    private String cbrUrl;

    public void loadCurrencies() {
        try {
            log.info("Starting currency load from CBR");

            RestTemplate restTemplate = new RestTemplate();
            String xmlData = restTemplate.getForObject(cbrUrl, String.class);

            JAXBContext jaxbContext = JAXBContext.newInstance(ValuteCursList.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            ValuteCursList valuteCursList = (ValuteCursList) unmarshaller.unmarshal(new StringReader(xmlData));

            List<ValuteCurs> valutes = valuteCursList.getValutes();
            log.info("Loaded {} currencies from CBR", valutes.size());

            for (ValuteCurs valute : valutes) {
                Currency existingCurrency = repository.findByIsoCharCode(valute.getCharCode());

                if (existingCurrency != null) {
                    existingCurrency.setName(valute.getName());
                    existingCurrency.setNominal(valute.getNominal());
                    existingCurrency.setValue(parseValue(valute.getValue()));
                    existingCurrency.setIsoNumCode(valute.getNumCode());
                    repository.save(existingCurrency);
                    log.debug("Updated currency: {}", valute.getCharCode());
                } else {
                    Currency newCurrency = new Currency();
                    newCurrency.setName(valute.getName());
                    newCurrency.setNominal(valute.getNominal());
                    newCurrency.setValue(parseValue(valute.getValue()));
                    newCurrency.setIsoNumCode(valute.getNumCode());
                    newCurrency.setIsoCharCode(valute.getCharCode());
                    repository.save(newCurrency);
                    log.debug("Created new currency: {}", valute.getCharCode());
                }
            }

            log.info("Currency load completed successfully");

        } catch (Exception e) {
            log.error("Failed to load currencies from CBR", e);
        }
    }

    private Double parseValue(String value) {
        return Double.parseDouble(value.replace(",", "."));
    }

    @Scheduled(fixedRate = 3600000)
    public void scheduledLoadCurrencies() {
        loadCurrencies();
    }

    @PostConstruct
    public void init() {
        loadCurrencies();
    }
}
