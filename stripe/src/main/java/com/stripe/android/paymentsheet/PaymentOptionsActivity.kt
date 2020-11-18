package com.stripe.android.paymentsheet

import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.stripe.android.databinding.StripeActivityPaymentOptionsBinding
import com.stripe.android.paymentsheet.ui.BasePaymentSheetActivity
import com.stripe.android.paymentsheet.ui.SheetMode
import com.stripe.android.paymentsheet.ui.Toolbar

/**
 * An `Activity` for selecting a payment option.
 */
internal class PaymentOptionsActivity : BasePaymentSheetActivity<PaymentOptionResult>() {
    @VisibleForTesting
    internal val viewBinding by lazy {
        StripeActivityPaymentOptionsBinding.inflate(layoutInflater)
    }

    @VisibleForTesting
    internal var viewModelFactory: ViewModelProvider.Factory =
        PaymentOptionsViewModel.Factory(
            { application },
            { requireNotNull(starterArgs) }
        )

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            viewModelFactory
        )[PaymentOptionsViewModel::class.java]
    }

    private val starterArgs: PaymentOptionsActivityStarter.Args? by lazy {
        PaymentOptionsActivityStarter.Args.fromIntent(intent)
    }

    @VisibleForTesting
    internal val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(viewBinding.bottomSheet)
    }

    private val bottomSheetController: BottomSheetController by lazy {
        BottomSheetController(
            bottomSheetBehavior = bottomSheetBehavior,
            sheetModeLiveData = viewModel.sheetMode,
            lifecycleScope
        )
    }

    private val fragmentContainerId: Int
        @IdRes
        get() = viewBinding.fragmentContainer.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val starterArgs = this.starterArgs
        if (starterArgs == null) {
            finish()
            return
        }
        val fragmentArgs = bundleOf(EXTRA_STARTER_ARGS to starterArgs)

        setContentView(viewBinding.root)

        // Handle taps outside of bottom sheet
        viewBinding.root.setOnClickListener {
            onUserCancel()
        }
        viewModel.error.observe(this) {
            animateOut(
                PaymentOptionResult.Failed(it)
            )
        }
        viewModel.sheetMode.observe(this) { mode ->
            when (mode) {
                SheetMode.Full,
                SheetMode.FullCollapsed -> {
                    viewBinding.toolbar.showBack()
                }
                SheetMode.Wrapped -> {
                    viewBinding.toolbar.showClose()
                }
                else -> {
                    // mode == null
                }
            }

            viewBinding.bottomSheet.layoutParams = viewBinding.bottomSheet.layoutParams.apply {
                height = mode.height
            }

            bottomSheetController.updateState(mode)
        }
        bottomSheetController.shouldFinish.observe(this) { shouldFinish ->
            if (shouldFinish) {
                finish()
            }
        }
        bottomSheetController.setup()

        setupPrimaryButton()

        viewModel.transition.observe(this) { transitionTarget ->
            if (transitionTarget != null) {
                onTransitionTarget(transitionTarget, fragmentArgs)
            }
        }

        viewModel.transitionTo(
            if (starterArgs.paymentMethods.isEmpty()) {
                PaymentOptionsViewModel.TransitionTarget.AddPaymentMethodSheet
            } else {
                PaymentOptionsViewModel.TransitionTarget.SelectSavedPaymentMethod
            }
        )

        viewBinding.toolbar.action.observe(this) { action ->
            if (action != null) {
                when (action) {
                    Toolbar.Action.Close -> {
                        onUserCancel()
                    }
                    Toolbar.Action.Back -> {
                        // TODO: implement back
                    }
                }
            }
        }
    }

    private fun setupPrimaryButton() {
        // TODO: implement
    }

    private fun onTransitionTarget(
        transitionTarget: PaymentOptionsViewModel.TransitionTarget,
        fragmentArgs: Bundle
    ) {
        supportFragmentManager.commit {
            when (transitionTarget) {
                PaymentOptionsViewModel.TransitionTarget.AddPaymentMethodFull -> {
                    // TODO: implement
                }
                PaymentOptionsViewModel.TransitionTarget.SelectSavedPaymentMethod -> {
                    // TODO: implement
                }
                PaymentOptionsViewModel.TransitionTarget.AddPaymentMethodSheet -> {
                    // TODO: implement
                }
            }
        }
        viewModel.updateMode(transitionTarget.sheetMode)
    }

    override fun setActivityResult(result: PaymentOptionResult) {
        setResult(
            result.resultCode,
            Intent()
                .putExtras(result.toBundle())
        )
    }

    override fun onUserCancel() {
        animateOut(
            PaymentOptionResult.Cancelled(
                mostRecentError = viewModel.error.value
            )
        )
    }

    override fun hideSheet() {
        bottomSheetController.hide()
    }
}
