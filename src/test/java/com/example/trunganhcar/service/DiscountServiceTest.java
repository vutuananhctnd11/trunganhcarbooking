package com.example.trunganhcar.service;

import com.example.trunganhcar.dto.DiscountRequest;
import com.example.trunganhcar.dto.DiscountResponse;
import com.example.trunganhcar.dto.DiscountValidateResponse;
import com.example.trunganhcar.entity.Discount;
import com.example.trunganhcar.exception.CustomException;
import com.example.trunganhcar.repository.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private DiscountService discountService;

    private Discount sampleFixedDiscount;
    private Discount samplePercentDiscount;

    @BeforeEach
    void setUp() {
        sampleFixedDiscount = Discount.builder()
                .id(1L)
                .code("TRUNGANH50K")
                .title("Giảm 50.000đ")
                .discountType("FIXED_AMOUNT")
                .discountValue(BigDecimal.valueOf(50000))
                .minTripPrice(BigDecimal.valueOf(200000))
                .isActive(true)
                .usedCount(0)
                .build();

        samplePercentDiscount = Discount.builder()
                .id(2L)
                .code("SALE10")
                .title("Giảm 10% tối đa 100K")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(10))
                .maxDiscountAmount(BigDecimal.valueOf(100000))
                .minTripPrice(BigDecimal.valueOf(150000))
                .isActive(true)
                .usedCount(0)
                .build();
    }

    @Test
    @DisplayName("Tạo mã giảm giá mới thành công")
    void createDiscount_success() {
        DiscountRequest request = DiscountRequest.builder()
                .code("NEWCODE")
                .title("Khuyến mãi mới")
                .discountType("FIXED_AMOUNT")
                .discountValue(BigDecimal.valueOf(30000))
                .build();

        when(discountRepository.existsByCodeIgnoreCase("NEWCODE")).thenReturn(false);
        when(discountRepository.save(any(Discount.class))).thenAnswer(invocation -> {
            Discount d = invocation.getArgument(0);
            d.setId(10L);
            return d;
        });

        DiscountResponse response = discountService.createDiscount(request);

        assertNotNull(response);
        assertEquals("NEWCODE", response.getCode());
        verify(discountRepository, times(1)).save(any(Discount.class));
    }

    @Test
    @DisplayName("Tạo mã giảm giá thất bại khi code đã tồn tại")
    void createDiscount_duplicateCode_throwsException() {
        DiscountRequest request = DiscountRequest.builder()
                .code("TRUNGANH50K")
                .title("Trùng mã")
                .discountValue(BigDecimal.valueOf(50000))
                .build();

        when(discountRepository.existsByCodeIgnoreCase("TRUNGANH50K")).thenReturn(true);

        assertThrows(CustomException.class, () -> discountService.createDiscount(request));
    }

    @Test
    @DisplayName("Validate mã giảm giá FIXED_AMOUNT thành công và tính đúng giá")
    void validateDiscount_fixedAmount_success() {
        when(discountRepository.findByCodeIgnoreCase("TRUNGANH50K")).thenReturn(Optional.of(sampleFixedDiscount));

        DiscountValidateResponse response = discountService.validateAndCalculateDiscount("TRUNGANH50K", BigDecimal.valueOf(300000));

        assertTrue(response.isValid());
        assertEquals(BigDecimal.valueOf(300000), response.getOriginalPrice());
        assertEquals(BigDecimal.valueOf(50000), response.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(250000), response.getFinalPrice());
    }

    @Test
    @DisplayName("Validate mã giảm giá PERCENTAGE thành công và bị chặn bởi mức giảm tối đa")
    void validateDiscount_percentage_cappedByMaxAmount() {
        when(discountRepository.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(samplePercentDiscount));

        // 10% của 1.500.000 là 150.000, nhưng maxDiscountAmount là 100.000
        DiscountValidateResponse response = discountService.validateAndCalculateDiscount("SALE10", BigDecimal.valueOf(1500000));

        assertTrue(response.isValid());
        assertEquals(BigDecimal.valueOf(100000), response.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(1400000), response.getFinalPrice());
    }

    @Test
    @DisplayName("Validate thất bại khi chưa đạt giá trị đơn tối thiểu")
    void validateDiscount_minPriceNotMet_throwsException() {
        when(discountRepository.findByCodeIgnoreCase("TRUNGANH50K")).thenReturn(Optional.of(sampleFixedDiscount));

        // minTripPrice là 200.000, chuyến chỉ có 150.000
        assertThrows(CustomException.class, () -> 
                discountService.validateAndCalculateDiscount("TRUNGANH50K", BigDecimal.valueOf(150000)));
    }

    @Test
    @DisplayName("Validate thất bại khi mã đã hết hạn")
    void validateDiscount_expired_throwsException() {
        sampleFixedDiscount.setEndAt(LocalDateTime.now().minusDays(1)); // Đã hết hạn hôm qua
        when(discountRepository.findByCodeIgnoreCase("TRUNGANH50K")).thenReturn(Optional.of(sampleFixedDiscount));

        assertThrows(CustomException.class, () -> 
                discountService.validateAndCalculateDiscount("TRUNGANH50K", BigDecimal.valueOf(300000)));
    }
}

