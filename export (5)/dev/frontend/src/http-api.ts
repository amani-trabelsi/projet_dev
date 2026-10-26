import axios, { type AxiosResponse } from 'axios';
import type { ImageType } from './image';

const instance = axios.create({
  baseURL: "/",
  timeout: 150000000,
});

const responseBody = (response: AxiosResponse) => response.data;

const requests = {
  get: (url: string, param: {}) => instance.get(url, param).then(responseBody),
  post: (url: string, body: {}) => instance.post(url, body, { headers: { "Content-Type": "multipart/form-data" }, }).then(responseBody),
  put: (url: string, body: {}) => instance.put(url, body).then(responseBody),
  delete: (url: string) => instance.delete(url).then(responseBody)
};

export const api = {
  getImageList: (): Promise<ImageType[]> => requests.get('images', {}),
  getImage: (id: number): Promise<Blob> => requests.get(`images/${id}`, { responseType: "blob" }),
  createImage: (form: FormData): Promise<ImageType> => requests.post('images', form),
  deleteImage: (id: number): Promise<void> => requests.delete(`images/${id}`),
  getSimilarImages: (id: number, number: number, descriptor: string): Promise<any> =>
    requests.get(`images/${id}/similar`, { params: { number, descriptor } }),
  login:(email:String,password:string): Promise<any> => requests.post('images/login',{email,password}),
  signup: (email: String, password: string, name: string): Promise<any> => 
    requests.post(`images/signup`, { email, password, name }),

  likeImage: (id: number, email: string, name: string, password: string): Promise<any> =>
    requests.post(`images/${id}/like`, { email, name, password}),

  unlikeImage: (id: number, email: string, name: string, password: string): Promise<string> => 
    requests.delete(`images/${id}/like?email=${email}&name=${name}&password=${password}`),

  getLikedImages: (email: string, password: string): Promise<ImageType[]> =>
    requests.get('images/liked', { params: { email, password} }),

  adjustBrightness: (id: number, delta: number): Promise<any> => 
    requests.get(`images/${id}/brightness`, { params: { delta } })
};