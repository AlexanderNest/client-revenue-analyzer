package ru.nesterov.web.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Value
@Schema(description = "Ответ со значением средней стоимости встречи")
public class AveragePriceResponse {
    @Schema(description = "Рассчитанная средняя стоимость занятия")
    Double averagePrice;
}
