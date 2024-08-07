package com.stripe.android.paymentsheet

import android.os.Parcelable
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.PaymentMethodOptionsParams
import kotlinx.parcelize.Parcelize

internal sealed interface PaymentConfirmationOption : Parcelable {
    @Parcelize
    data class Saved(
        val paymentMethod: PaymentMethod,
        val optionsParams: PaymentMethodOptionsParams?,
    ) : PaymentConfirmationOption

    @Parcelize
    data class GooglePay(
        val config: Config,
    ) : PaymentConfirmationOption {
        @Parcelize
        data class Config(
            val environment: PaymentSheet.GooglePayConfiguration.Environment?,
            val merchantName: String,
            val merchantCountryCode: String,
            val merchantCurrencyCode: String?,
            val customAmount: Long?,
            val customLabel: String?,
            val billingDetailsCollectionConfiguration: PaymentSheet.BillingDetailsCollectionConfiguration,
        ) : Parcelable
    }

    @Parcelize
    data class ExternalPaymentMethod(
        val type: String,
        val billingDetails: PaymentMethod.BillingDetails?,
    ) : PaymentConfirmationOption

    @Parcelize
    data class New(
        val createParams: PaymentMethodCreateParams,
        val optionsParams: PaymentMethodOptionsParams?,
        val shouldSave: Boolean
    ) : PaymentConfirmationOption
}
