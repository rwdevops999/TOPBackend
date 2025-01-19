package com.tutopedia.backend.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutopedia.backend.persistence.data.TutorialWithFile;
import com.tutopedia.backend.persistence.model.Repository;
import com.tutopedia.backend.persistence.model.Setting;
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.persistence.model.TutorialFile;
import com.tutopedia.backend.persistence.repository.RepositoryRepository;
import com.tutopedia.backend.persistence.repository.SettingRepository;
import com.tutopedia.backend.persistence.repository.TutorialFileRepository;
import com.tutopedia.backend.persistence.repository.TutorialRepository;

@Service
public class CommandService {
	@Autowired
	private TutorialRepository tutorialRepository;

	@Autowired
	private RepositoryRepository repoRepository;

	@Autowired
	private SettingRepository settingRepository;

	@Autowired
	private TutorialFileRepository fileRepository;

	private Tutorial persistTutorial(Tutorial tutorial) {
		return tutorialRepository.save(tutorial);
	}
	
	public Tutorial createTutorial(Tutorial tutorial) {
		return persistTutorial(tutorial);
	}
	
	public Tutorial updateTutorial(Tutorial tutorial) {
		return persistTutorial(tutorial);
	}
	
	public void publishAllTutorials() {
		tutorialRepository.publishAll();
	}

	public void publishTutorialById(long id) {
		tutorialRepository.publishById(id);
	}
	
	public void deleteAllTutorials() {
		tutorialRepository.deleteAll();
	}

	public void deleteTutorialById(long id) {
		tutorialRepository.deleteById(id);
	}
	
	public void deleteAllRepositories() {
		repoRepository.deleteAll();
	}

	public void deleteRepositoryById(long id) {
		repoRepository.deleteById(id);
	}

	public Repository saveRepository(Repository repository) {
		return repoRepository.save(repository);
	}

	public Repository createRepository(Repository repository) {
		return saveRepository(repository);
	}

	public void updateDefaultRepositoryId(long id) {
		repoRepository.clearDefaultRepositories();
		repoRepository.updateDefaultRepositoryId(id);
	}
	
	public void persistSetting(Setting setting) {
		settingRepository.save(setting);
	}
}
