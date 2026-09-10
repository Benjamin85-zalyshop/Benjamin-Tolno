sed -i '/fun fixPrimarySubjectsMaxScore/,$d' app/src/main/java/com/example/ui/SchoolViewModel.kt
cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/SchoolViewModel.kt
    fun fixPrimarySubjectsMaxScore() {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val allSubjects = repository.getAllSubjectsDirect(_currentSchoolId.value ?: return@launch)
            for (subject in allSubjects) {
                if ((subject.section == "LE PRIMAIRE" || subject.section == "LA MATERNELLE") && subject.maxScore != 10f) {
                    val fixedSubject = subject.copy(maxScore = 10f)
                    repository.updateSubject(fixedSubject)
                    if (fixedSubject.remoteId.isNotEmpty()) {
                        firestore.collection("schools").document(email).collection("subjects").document(fixedSubject.remoteId).update(
                            "maxScore", 10f
                        )
                    }
                }
            }
        }
    }
}

class SchoolViewModelFactory(
    private val repository: SchoolRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchoolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchoolViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
INNER_EOF
