/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_CONCURRENTQUEUE_H_
#define CAMEO_CONCURRENTQUEUE_H_

#include "Timeout.h"
#include <queue>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <memory>

namespace cameo {

/**
 * Class implementing a concurrent queue. This is a modified version of the implementation:
 * https://juanchopanzacpp.wordpress.com/2013/02/26/concurrent-queue-c11/
 * Supports only pointer types.
 */
template<typename Type>
class CAMEO_EXPORT ConcurrentQueue {

public:
	/**
	 * Destructor. Deletes all the items.
	 */
	~ConcurrentQueue() {

		std::unique_lock<std::mutex> lock(m_mutex);

		while (!m_queue.empty()) {
			delete m_queue.front();
			m_queue.pop();
		}
	}

	/**
	 * Gets the front item if the queue is not empty. Returns a null pointer otherwise.
	 * \return An item that is null if the queue is empty.
	 */
	std::unique_ptr<Type> poll() {

		std::unique_lock<std::mutex> lock(m_mutex);

		if (m_queue.empty()) {
			return std::unique_ptr<Type>();
		}
		auto item = m_queue.front();
		m_queue.pop();

		return std::unique_ptr<Type>(item);
	}

	/**
	 * Gets the front item. Blocking call until there is an item.
	 * \return An item that cannot be null.
	 */
	std::unique_ptr<Type> pop(int timeout = -1) {

		std::unique_lock<std::mutex> lock(m_mutex);

		while (m_queue.empty()) {

			if (timeout == -1) {
				m_condition.wait(lock);
			}
			else {
				std::cv_status status = m_condition.wait_for(lock, std::chrono::milliseconds(timeout));
				if (status == std::cv_status::timeout) {
					throw Timeout();
				}
			}
		}
		auto item = m_queue.front();
		m_queue.pop();

		return std::unique_ptr<Type>(item);
	}

	/**
	 * Pushes an item.
	 * \param item An item.
	 */
	void push(std::unique_ptr<Type> & item) {

		std::unique_lock<std::mutex> lock(m_mutex);
		m_queue.push(item.release());
		lock.unlock();
		m_condition.notify_one();
	}

	/**
	 * Returns the size of the queue.
	 * \return The size of the queue.
	 */
	typename std::queue<Type *>::size_type size() {

		std::unique_lock<std::mutex> lock(m_mutex);
		return m_queue.size();
	}

private:
	std::queue<Type *> m_queue;
	std::mutex m_mutex;
	std::condition_variable m_condition;
};

}

#endif