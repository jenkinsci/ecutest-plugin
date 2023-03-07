/**
 * SPDX-FileCopyrightText: 2015-2019 TraceTronic GmbH <info@tracetronic.de>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

buildPlugin(
  useContainerAgent: true, // Set to `false` if you need to use Docker for containerized tests
  configurations: [
    [platform: 'linux', jdk: 11],
    [platform: 'windows', jdk: 11],
    [platform: 'linux', jdk: 17],
    [platform: 'windows', jdk: 17]
  ],
  useArtifactCachingProxy: false // Use plugins.jenkins.io directly
)
