package com.example.divulgenewsapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divulgenewsapp.adapters.ExclusiveNewsPlansAdapter
import com.example.divulgenewsapp.databinding.ActivityNewsSubscriptionBinding
import com.example.divulgenewsapp.models.ExclusiveNewsPlan
import com.example.divulgenewsapp.models.Subscription
import com.example.divulgenewsapp.repository.SubscriptionRepository
import com.example.divulgenewsapp.ui.viewmodel.SubscriptionViewModel
import com.example.divulgenewsapp.ui.viewmodel.SubscriptionViewModelProviderFactory
import com.example.divulgenewsapp.util.UiState
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.time.LocalDate

class NewsSubscriptionActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityNewsSubscriptionBinding
    private lateinit var plansAdapter: ExclusiveNewsPlansAdapter
    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var selectedPlan: ExclusiveNewsPlan

    companion object {
        const val TAG = "NewsSubscriptionActivity"
        const val YOUR_CLIENT_ID = "Aep7fQGlPoW5hOlOuPSYipe92E7iwly0Qet1YrcE9q1JATlDcoHqpyv3viW3N0_OS6nJ6SX0P_rhh2bi"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsSubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Instantiate the news repository.
        val subscriptionRepository = SubscriptionRepository()
        val viewModelProviderFactory = SubscriptionViewModelProviderFactory(application, subscriptionRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[SubscriptionViewModel::class.java]

        viewModel.getExclusiveNewsPlansFromFireStore()
        viewModel.exclusiveNewsPlansFromFireStore.observe(this, Observer {
            setupRecyclerView(it)
        })

        binding.btnPurchaseNow.setOnClickListener {
            viewModel.checkUserHaveAnyActiveSubscription {
                when (it) {
                    is UiState.Failure -> {
                        Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show()
                    }
                    UiState.Loading -> {}
                    is UiState.Success -> {
                        if (it.data) {
                            Toast.makeText(this, "Subscription exists", Toast.LENGTH_SHORT).show()
                        } else {
                            try {
                                if (selectedPlan != null) {
                                    saveSubscriptionData(selectedPlan)
                                } else {
                                    Toast.makeText(this, "Please select any plan", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: UninitializedPropertyAccessException) {
                                Toast.makeText(this, "Please select any plan", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun payForSubscription(amount: String) {
        val co = Checkout()

        try {
            val options = JSONObject()
            options.put("name", "Divulge News")
            options.put("description", "Exclusive News Subscription")
            //You can omit the image option to fetch the image from the dashboard
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", amount)//pass amount in currency subunits

            val prefill = JSONObject()
            prefill.put("email", "")
            prefill.put("contact", "")

            options.put("prefill", prefill)
            co.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupRecyclerView(list: List<ExclusiveNewsPlan>) {
        plansAdapter = ExclusiveNewsPlansAdapter(this, list)
        binding.recyclerView.apply {
            adapter = plansAdapter
            layoutManager = LinearLayoutManager(this@NewsSubscriptionActivity)
            // addItemDecoration(DividerItemDecoration(this@NewsSubscriptionActivity, LinearLayoutManager.VERTICAL))
        }

        plansAdapter.setOnItemClickListener {
            selectedPlan = it
        }
    }

    private fun saveSubscriptionData(plan: ExclusiveNewsPlan) {
        val amountForPayment = (plan.price).toString().replaceFirst(".", "")
        payForSubscription(amountForPayment)
    }

    private fun saveSubscriptionInFirebase(plan: ExclusiveNewsPlan) {
        val subscription = Subscription(
            null,
            FirebaseAuth.getInstance().uid,
            plan.id,
            LocalDate.now().toString(),
            LocalDate.now().plusMonths(plan.numberOfMonths!!.toLong()).toString()
        )

        viewModel.saveSubscriptionData(subscription) {
            when (it) {
                is UiState.Failure -> {
                    Toast.makeText(this, "Failed to add subscription", Toast.LENGTH_SHORT).show()
                }
                UiState.Loading -> {}
                is UiState.Success<*> -> {
                    Toast.makeText(this@NewsSubscriptionActivity, "Subscription Added", Toast.LENGTH_SHORT).show()
                    finish()
                    //navigateToExclusiveNewsFragment()
                }
            }
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        saveSubscriptionInFirebase(selectedPlan)
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this, "Payment is failed", Toast.LENGTH_SHORT).show()
    }

}
