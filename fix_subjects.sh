cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/SchoolViewModel.kt

    fun fixPrimarySubjectsMaxScore() {
        val email = _schoolAccount.value?.schoolName ?: return
        viewModelScope.launch {
            val allSubjects = repository.getAllSubjectsDirect()
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
INNER_EOF
