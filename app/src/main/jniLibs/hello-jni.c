/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <jni.h>
#include <stdlib.h>
/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */
int GetLCS(const jchar* _A, int _A_Length, const jchar* _B, int _B_Length, jchar* out) {
	int ix= 0;
	/*int _A_Length= strlen(_A);
	int _B_Length= strlen(_B);*/
	int sw= 0;
	if (_A_Length > _B_Length) {
		const jchar* t= _B;
		_B            = _A;
		_A            = t;
		int g         = _B_Length;
		_B_Length     = _A_Length;
		_A_Length     = g;
		sw            = 1;
	}
	int p= *out ? _A_Length + 8 : 3;
	int L[_A_Length + 1], X[_A_Length + 1], S[_A_Length + 1], T[_A_Length + 1], i, j;
	L[0]= X[0]= -1;
	for (i= 1; i <= _A_Length; ++i) L[i]= X[i]= T[i]= __INT_MAX__;
	for (i= 0; i < _A_Length; ++i) {
		for (j= 1; j <= _A_Length - i; ++j) {
			// T= GetP(i + j - 1, L[j - 1], L[j]);
			// T        = INT_MAX;
			const int Index= i + j - 1;
			const int EndP = L[j] == __INT_MAX__ ? _B_Length : L[j];
			const jchar ac  = _A[Index];
			// if (EndP == INT_MAX) EndP= _B_Length;
			if (ac == ' ') {
				if (L[j] == __INT_MAX__)
					break;
				else
					continue;
			}
			for (int i= L[j - 1] + 1; i < EndP; ++i){
				// for (int i= EndP - 1; i > L[j - 1]; --i)
				//if (_B[i] == ' ') continue;
				if (ac == _B[i]) {
					if (L[j] == __INT_MAX__) {
						if (T[j - 1] != __INT_MAX__) {
							int fj= j;
							while (T[--fj] != __INT_MAX__) {
								if (T[fj] > L[fj]) T[fj]= __INT_MAX__;
								if (fj <= 0) break;
							}
						}
					} else {
						if (T[j] == __INT_MAX__) {
							S[j]= X[j];
							T[j]= L[j];
						}
					}
					X[j]= Index;
					L[j]= i;
					break;
				}
			}
			if (L[j] == __INT_MAX__) break;
		}
		if (L[_A_Length - i] != __INT_MAX__) {
			for (j= _A_Length - i; j > 1; --j) {
				const int p= j - 1;
				if (T[p] != __INT_MAX__) {
					if (L[p] >= L[j] || X[p] >= X[j]) {
						L[p]= T[p];
						X[p]= S[p];
					}
				}
			}
			for (j= _A_Length - i; j > 1; --j) {
				int mov= 1;
				while (j > mov) {
					int ch= 0;
					if (X[j - mov] != X[j] - mov && _A[X[j - mov]] == _A[X[j] - mov]) {
						X[j - mov]= X[j] - mov;
						ch        = 1;
					}
					if (L[j - mov] != L[j] - mov && _B[L[j - mov]] == _B[L[j] - mov]) {
						L[j - mov]= L[j] - mov;
						ch+= 2;
					}
					if (ch == 3) {
						++mov;
					} else {
						j-= mov - 1;
						break;
					}
				}
			}
			for (j= 1; j <= _A_Length - i; ++j) {
				if (sw) {
					if (L[j] != L[j - 1] + 1 && j > 1) {
						out[ix++]= ' ';
					}
					out[ix++]= _B[L[j]];
				} else {
					if (X[j] != X[j - 1] + 1 && j > 1) {
						out[ix++]= ' ';
					}
					out[ix++]= _A[X[j]];
				}
			}
			out[ix++]= '\2';
			for (j= 1; j <= _A_Length - i; ++j) {
				if (sw) {
					if (X[j] != X[j - 1] + 1 && j > 1) {
						out[ix++]= ' ';
					}
					out[ix++]= _A[X[j]];
				} else {
					if (L[j] != L[j - 1] + 1 && j > 1) {
						out[ix++]= ' ';
					}
					out[ix++]= _B[L[j]];
				}
			}
			out[ix]  = 0;
			break;
		}
	}
	/*for (j= 1; j <= _A_Length - i; ++j) {
		gotoxy(L[j] * 2 + 4, X[j] + p + 1);
		printf("%c", _B[L[j]]);
	}*/
	L[0]= X[0]= -2;
	{
		int lenA= 0, lenB= 0;
		int numA= 0, numB= 0;
		for (j= 1; j <= _A_Length - i; ++j) {
			int x= L[j] - L[j - 1];
			int y= X[j] - X[j - 1];
			if (x == 1)
				++lenA;
			else if (j > 1) {
				lenA= 0;
				++numA;
			}
			if (x == 1)
				++lenB;
			else if (j > 1) {
				lenB= 0;
				++numB;
			}
		}
		if (lenA) ++numA;
		if (lenB) ++numB;
	}
	return ix;
}
JNIEXPORT jstring JNICALL Java_org_lcs_MainActivity_stringFromJNI(JNIEnv* env, jclass cls, jstring a, jstring b) {
	jboolean _copy;
	const jchar* A= (*env)->GetStringChars(env, a, &_copy);
	const jchar* B= (*env)->GetStringChars(env, b, &_copy);
	int i= (*env)->GetStringLength(env, a), j= (*env)->GetStringLength(env, b);
	jchar out[(i < j ? i : j) * 6 + 2];
	out[0] = 0;
	int len= GetLCS(A, i, B, j, out);
	(*env)->ReleaseStringChars(env, a, A);
	(*env)->ReleaseStringChars(env, b, B);
	return (*env)->NewString(env, out, len);
}
