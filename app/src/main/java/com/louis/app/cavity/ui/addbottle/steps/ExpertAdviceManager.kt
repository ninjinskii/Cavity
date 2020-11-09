package com.louis.app.cavity.ui.addbottle.steps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.db.WineRepository
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpertAdviceManager (
    private val repository: WineRepository,
    private val _userFeedback: MutableLiveData<Event<Int>>,
    private val viewModelScope: CoroutineScope
) {
    private val _expertAdvices = MutableLiveData<MutableList<ExpertAdvice>>()
    val expertAdvices: LiveData<MutableList<ExpertAdvice>>
        get() = _expertAdvices

    fun addExpertAdvice(contestName: String, typeToVal: Pair<AdviceType, Int>) {
        if (contestName.isEmpty()) {
            _userFeedback.postOnce(R.string.empty_contest_name)
            return
        }

        if (alreadyContainsAdvice(contestName)) {
            _userFeedback.postOnce(R.string.contest_name_already_exist)
            return
        }

        val advice: ExpertAdvice? = when (typeToVal.first) {
            AdviceType.RATE_20 -> {
                if (checkRateInBounds(typeToVal.second, 20)) {
                    ExpertAdvice(0, contestName, 0, 0, 1, 0, typeToVal.second, -1)
                } else {
                    _userFeedback.postOnce(R.string.rate_out_of_bounds)
                    null
                }
            }
            AdviceType.RATE_100 -> {
                if (checkRateInBounds(typeToVal.second, 100)) {
                    ExpertAdvice(0, contestName, 0, 0, 0, 1, typeToVal.second, -1)
                } else {
                    _userFeedback.postOnce(R.string.rate_out_of_bounds)
                    null
                }
            }
            AdviceType.MEDAL -> ExpertAdvice(0, contestName, 1, 0, 0, 0, typeToVal.second, -1)
            else -> ExpertAdvice(0, contestName, 0, 1, 0, 0, typeToVal.second, -1)
        }

        advice?.let { adv -> _expertAdvices += adv }
    }

    fun removeExpertAdvice(advice: ExpertAdvice) {
        // Deleted expert advice might already be in database, need to remove it
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAdvice(advice)
        }

        _expertAdvices -= advice
    }

    fun postValue(value: MutableList<ExpertAdvice>) {
        _expertAdvices.postValue(value)
    }

    fun reset() {
        _expertAdvices.postValue(mutableListOf())
    }

    private fun checkRateInBounds(rate: Int, max: Int) = rate in 0..max

    private fun alreadyContainsAdvice(contestName: String): Boolean {
        val advicesName = _expertAdvices.value?.map { it.contestName } ?: return false
        return contestName in advicesName
    }
}
